package com.mobdirector;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

/**
 * Comandos para dirigir mobs. Todos aceptan selectores de entidad normales
 * (incluidas etiquetas), por ejemplo {@code @e[tag=guardias]}.
 *
 *   /mobctl attack <atacantes> <objetivo> [persistente]  -> atacan al objetivo
 *   /mobctl follow <mobs> <objetivo> [vel]               -> siguen a una entidad en movimiento
 *   /mobctl moveto <mobs> <x y z> [vel]                  -> caminan hacia una posicion
 *   /mobctl glide  <mobs> <x y z> <ticks>                -> se deslizan (cinematica) a una posicion
 *   /mobctl look   <mobs> <x y z>                        -> miran hacia un punto
 *   /mobctl stop   <mobs>                                -> olvidan objetivo, ruta y directivas
 */
public final class MobCommands {

	private MobCommands() {
	}

	/** Nivel de versión reconstruida: 1=1.0.0, 2=1.1.0, 3=1.2.0, 4=1.3.0, 5=1.4.0/1.5.0. */
	public static final int LEVEL = 5;

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> root =
				Commands.literal("mobctl").requires(source -> source.hasPermission(2));

		// --- 1.0.0: attack, moveto, stop ---
		var attackTarget = Commands.argument("target", EntityArgument.entity())
				.executes(ctx -> attack(ctx, false));
		if (LEVEL >= 2) { // 1.1.0 añadió el objetivo persistente
			attackTarget.then(Commands.argument("persistent", BoolArgumentType.bool())
					.executes(ctx -> attack(ctx, BoolArgumentType.getBool(ctx, "persistent"))));
		}
		root.then(Commands.literal("attack")
				.then(Commands.argument("attackers", EntityArgument.entities())
						.then(attackTarget)));
		root.then(Commands.literal("moveto")
				.then(Commands.argument("mobs", EntityArgument.entities())
						.then(Commands.argument("pos", Vec3Argument.vec3())
								.executes(ctx -> moveTo(ctx, 1.0))
								.then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.1, 10.0))
										.executes(ctx -> moveTo(ctx, DoubleArgumentType.getDouble(ctx, "speed")))))));
		root.then(Commands.literal("stop")
				.then(Commands.argument("mobs", EntityArgument.entities())
						.executes(MobCommands::stop)));

		// --- 1.1.0: follow, glide, look ---
		if (LEVEL >= 2) {
			root.then(Commands.literal("follow")
					.then(Commands.argument("mobs", EntityArgument.entities())
							.then(Commands.argument("target", EntityArgument.entity())
									.executes(ctx -> follow(ctx, 1.0))
									.then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.1, 10.0))
											.executes(ctx -> follow(ctx, DoubleArgumentType.getDouble(ctx, "speed")))))));

			var ticks = Commands.argument("ticks", IntegerArgumentType.integer(1, 12000))
					.executes(ctx -> glide(ctx, Directives.GlideMode.GROUND));
			if (LEVEL >= 5) { // 1.4.0 añadió los modos ground/air
				ticks.then(Commands.literal("ground").executes(ctx -> glide(ctx, Directives.GlideMode.GROUND)))
						.then(Commands.literal("air").executes(ctx -> glide(ctx, Directives.GlideMode.AIR)));
			}
			root.then(Commands.literal("glide")
					.then(Commands.argument("mobs", EntityArgument.entities())
							.then(Commands.argument("pos", Vec3Argument.vec3())
									.then(ticks))));

			root.then(Commands.literal("look")
					.then(Commands.argument("mobs", EntityArgument.entities())
							.then(Commands.argument("pos", Vec3Argument.vec3())
									.executes(MobCommands::look))));
		}

		// --- 1.2.0: select, deselect ---
		if (LEVEL >= 3) {
			root.then(Commands.literal("select")
					.then(Commands.argument("tag", StringArgumentType.word())
							.executes(MobCommands::select)));
			root.then(Commands.literal("deselect")
					.then(Commands.argument("tag", StringArgumentType.word())
							.executes(MobCommands::deselect)));
		}

		// --- 1.3.0: delay ---
		if (LEVEL >= 4) {
			root.then(Commands.literal("delay")
					.then(Commands.argument("seconds", DoubleArgumentType.doubleArg(0.0, 600.0))
							.then(Commands.argument("command", StringArgumentType.greedyString())
									.executes(MobCommands::delay))));
		}

		dispatcher.register(root);
	}

	/** Cada mob atacante fija como objetivo a la entidad indicada (opcionalmente de forma persistente). */
	private static int attack(CommandContext<CommandSourceStack> ctx, boolean persistent) throws CommandSyntaxException {
		Collection<? extends Entity> attackers = EntityArgument.getEntities(ctx, "attackers");
		Entity targetEntity = EntityArgument.getEntity(ctx, "target");

		if (!(targetEntity instanceof LivingEntity target)) {
			ctx.getSource().sendFailure(Component.literal("El objetivo debe ser una entidad viva."));
			return 0;
		}

		int count = 0;
		for (Entity entity : attackers) {
			if (entity instanceof Mob mob && mob != target) {
				Directives.clear(mob);
				mob.setTarget(target);
				mob.setPersistenceRequired();
				if (persistent) {
					Directives.attackPersistent(mob, target);
				}
				count++;
			}
		}

		final int total = count;
		final String targetName = target.getName().getString();
		final String mode = persistent ? " (persistente)" : "";
		ctx.getSource().sendSuccess(
				() -> Component.literal(total + " mob(s) atacando a " + targetName + mode), true);
		return count;
	}

	/** Cada mob sigue continuamente a la entidad indicada, aunque esta se mueva. */
	private static int follow(CommandContext<CommandSourceStack> ctx, double speed) throws CommandSyntaxException {
		Collection<? extends Entity> mobs = EntityArgument.getEntities(ctx, "mobs");
		Entity target = EntityArgument.getEntity(ctx, "target");

		int count = 0;
		for (Entity entity : mobs) {
			if (entity instanceof Mob mob && mob != target) {
				Directives.follow(mob, target, speed);
				count++;
			}
		}

		final int total = count;
		final String targetName = target.getName().getString();
		ctx.getSource().sendSuccess(
				() -> Component.literal(total + " mob(s) siguiendo a " + targetName), true);
		return count;
	}

	/** Cada mob camina (pathfinding) hacia la posicion indicada. */
	private static int moveTo(CommandContext<CommandSourceStack> ctx, double speed) throws CommandSyntaxException {
		Collection<? extends Entity> mobs = EntityArgument.getEntities(ctx, "mobs");
		Vec3 pos = Vec3Argument.getVec3(ctx, "pos");

		int count = 0;
		for (Entity entity : mobs) {
			if (entity instanceof Mob mob) {
				Directives.clear(mob);
				boolean started = mob.getNavigation().moveTo(pos.x, pos.y, pos.z, speed);
				if (started) {
					count++;
				}
			}
		}

		final int total = count;
		ctx.getSource().sendSuccess(
				() -> Component.literal(String.format("%d mob(s) moviendose hacia %.1f %.1f %.1f",
						total, pos.x, pos.y, pos.z)), true);
		return count;
	}

	/** Cada mob se desliza suavemente (cinematica) hacia la posicion en el numero de ticks dado. */
	private static int glide(CommandContext<CommandSourceStack> ctx, Directives.GlideMode mode) throws CommandSyntaxException {
		Collection<? extends Entity> mobs = EntityArgument.getEntities(ctx, "mobs");
		Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
		int ticks = IntegerArgumentType.getInteger(ctx, "ticks");

		int count = 0;
		for (Entity entity : mobs) {
			if (entity instanceof Mob mob) {
				Directives.glide(mob, pos, ticks, mode);
				count++;
			}
		}

		final int total = count;
		final double seconds = ticks / 20.0;
		final String modeLabel = mode == Directives.GlideMode.GROUND ? "sobre el suelo" : "por el aire";
		ctx.getSource().sendSuccess(
				() -> Component.literal(String.format("%d mob(s) deslizandose %s hacia %.1f %.1f %.1f en %.1f s",
						total, modeLabel, pos.x, pos.y, pos.z, seconds)), true);
		return count;
	}

	/** Cada mob gira para mirar hacia el punto indicado. */
	private static int look(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<? extends Entity> mobs = EntityArgument.getEntities(ctx, "mobs");
		Vec3 pos = Vec3Argument.getVec3(ctx, "pos");

		int count = 0;
		for (Entity entity : mobs) {
			if (entity instanceof Mob mob) {
				mob.lookAt(EntityAnchorArgument.Anchor.EYES, pos);
				count++;
			}
		}

		final int total = count;
		ctx.getSource().sendSuccess(
				() -> Component.literal(total + " mob(s) mirando hacia el punto"), true);
		return count;
	}

	/**
	 * Programa un comando para ejecutarse tras N segundos, con cuenta atras en la barra
	 * de accion. Sirve para colocar la camara antes de que el mob se mueva.
	 * Ejemplo: /mobctl delay 3 mobctl glide @e[tag=oso] -1212 62 2796 200
	 */
	private static int delay(CommandContext<CommandSourceStack> ctx) {
		double seconds = DoubleArgumentType.getDouble(ctx, "seconds");
		String command = StringArgumentType.getString(ctx, "command");
		if (command.startsWith("/")) {
			command = command.substring(1);
		}

		int ticks = (int) Math.round(seconds * 20.0);
		Scheduler.schedule(ctx.getSource(), command, ticks);

		final double s = seconds;
		ctx.getSource().sendSuccess(
				() -> Component.literal(String.format("Comando programado: se ejecutara en %.1f s", s)), false);
		return 1;
	}

	/**
	 * Etiqueta EXACTAMENTE al mob que el jugador esta mirando (raycast desde los ojos).
	 * Asi la etiqueta apunta al mob concreto, sin ambiguedad entre adulto y cria.
	 * Debe ejecutarlo un jugador. Alcance: 64 bloques.
	 */
	private static int select(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		String tag = StringArgumentType.getString(ctx, "tag");

		final double range = 64.0;
		Vec3 eye = player.getEyePosition();
		Vec3 view = player.getViewVector(1.0F);
		Vec3 end = eye.add(view.x * range, view.y * range, view.z * range);
		AABB searchBox = player.getBoundingBox().expandTowards(view.scale(range)).inflate(1.0);

		EntityHitResult hit = ProjectileUtil.getEntityHitResult(
				player, eye, end, searchBox,
				entity -> entity instanceof Mob && entity.isAlive(),
				range * range);

		if (hit == null || !(hit.getEntity() instanceof Mob mob)) {
			ctx.getSource().sendFailure(Component.literal(
					"No estas mirando a ningun mob (alcance " + (int) range + " bloques)."));
			return 0;
		}

		mob.addTag(tag);
		final String name = mob.getName().getString();
		ctx.getSource().sendSuccess(
				() -> Component.literal("Seleccionado " + name + " con la etiqueta '" + tag + "'"), true);
		return 1;
	}

	/**
	 * Quita una etiqueta de TODOS los mobs que la tengan y cancela sus directivas,
	 * dejandolos libres para volver a seleccionar/activar otra cosa.
	 */
	private static int deselect(CommandContext<CommandSourceStack> ctx) {
		String tag = StringArgumentType.getString(ctx, "tag");
		MinecraftServer server = ctx.getSource().getServer();

		int count = 0;
		for (ServerLevel level : server.getAllLevels()) {
			for (Entity entity : level.getAllEntities()) {
				if (entity.getTags().contains(tag)) {
					entity.removeTag(tag);
					if (entity instanceof Mob mob) {
						Directives.clear(mob);
						mob.setTarget(null);
						mob.getNavigation().stop();
					}
					count++;
				}
			}
		}

		final int total = count;
		ctx.getSource().sendSuccess(
				() -> Component.literal("Deseleccionados " + total + " mob(s) con la etiqueta '" + tag + "'"), true);
		return count;
	}

	/** Detiene a los mobs: cancela directivas, olvida objetivo y para la ruta actual. */
	private static int stop(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<? extends Entity> mobs = EntityArgument.getEntities(ctx, "mobs");

		int count = 0;
		for (Entity entity : mobs) {
			if (entity instanceof Mob mob) {
				Directives.clear(mob);
				mob.setTarget(null);
				mob.getNavigation().stop();
				count++;
			}
		}

		final int total = count;
		ctx.getSource().sendSuccess(
				() -> Component.literal("Detenidos " + total + " mob(s)"), true);
		return count;
	}
}
