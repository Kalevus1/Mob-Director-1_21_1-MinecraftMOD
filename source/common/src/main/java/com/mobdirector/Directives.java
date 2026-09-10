package com.mobdirector;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro central de "directivas" activas: comportamientos que hay que reaplicar
 * cada tick del servidor (seguir a una entidad, mantener un objetivo de ataque,
 * o deslizarse de forma cinematica hacia una posicion).
 *
 * Cada mob tiene como maximo UNA directiva activa; una nueva reemplaza a la anterior.
 * {@link MobDirector} llama a {@link #tick()} en cada END_SERVER_TICK.
 */
public final class Directives {

	/** Modo de {@code glide}. */
	public enum GlideMode {
		/** Sigue la altura del terreno: el mob queda SIEMPRE encima de los bloques. */
		GROUND,
		/** Interpola la Y literal indicada: para mobs voladores o tomas aereas. */
		AIR
	}

	private static final Map<UUID, Directive> ACTIVE = new ConcurrentHashMap<>();

	private Directives() {
	}

	// --- API publica que usan los comandos ---

	/** El mob sigue continuamente a la entidad objetivo (pathfinding, se actualiza si se mueve). */
	public static void follow(Mob mob, Entity target, double speed) {
		set(mob, new Follow(mob, target, speed));
	}

	/** El mob reafirma su objetivo de ataque cada tick, aunque su IA intente olvidarlo. */
	public static void attackPersistent(Mob mob, LivingEntity target) {
		set(mob, new Attack(mob, target));
	}

	/** El mob se desliza suavemente desde su posicion actual hasta {@code to} en {@code ticks} ticks. */
	public static void glide(Mob mob, Vec3 to, int ticks, GlideMode mode) {
		set(mob, new Glide(mob, to, ticks, mode));
	}

	/** Cancela la directiva activa de un mob (si tiene). */
	public static void clear(Mob mob) {
		Directive previous = ACTIVE.remove(mob.getUUID());
		if (previous != null) {
			previous.onClear();
		}
	}

	/** Se llama cada tick del servidor. */
	public static void tick() {
		if (ACTIVE.isEmpty()) {
			return;
		}
		Iterator<Map.Entry<UUID, Directive>> it = ACTIVE.entrySet().iterator();
		while (it.hasNext()) {
			Directive directive = it.next().getValue();
			if (directive.mob.isRemoved() || !directive.mob.isAlive()) {
				directive.onClear();
				it.remove();
				continue;
			}
			if (directive.tick()) {
				it.remove();
			}
		}
	}

	private static void set(Mob mob, Directive directive) {
		Directive previous = ACTIVE.put(mob.getUUID(), directive);
		if (previous != null) {
			previous.onClear();
		}
	}

	/** Orienta al mob (cuerpo + cabeza) hacia el vector de avance indicado. */
	private static void faceMovement(Mob mob, double dx, double dz, double dy) {
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		if (horizontal >= 1.0e-4) {
			float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
			mob.setYRot(yaw);
			mob.setYBodyRot(yaw);
			mob.setYHeadRot(yaw);
		}
		// Con horizontal ~0 (subida/bajada vertical pura) mantenemos el yaw actual.
		float pitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.max(horizontal, 1.0e-4))));
		mob.setXRot(pitch);
	}

	// --- Tipos de directiva ---

	private abstract static class Directive {
		final Mob mob;

		Directive(Mob mob) {
			this.mob = mob;
		}

		/** @return true cuando la directiva ha terminado y debe eliminarse. */
		abstract boolean tick();

		/** Se llama al cancelar o terminar la directiva; para restaurar estado. */
		void onClear() {
		}
	}

	/** Sigue a una entidad en movimiento reemitiendo la ruta periodicamente. */
	private static final class Follow extends Directive {
		private final Entity target;
		private final double speed;
		private int cooldown = 0;

		Follow(Mob mob, Entity target, double speed) {
			super(mob);
			this.target = target;
			this.speed = speed;
		}

		@Override
		boolean tick() {
			if (target.isRemoved() || !target.isAlive()) {
				return true;
			}
			if (cooldown-- <= 0) {
				mob.getNavigation().moveTo(target, speed);
				cooldown = 10; // recalcula la ruta cada medio segundo
			}
			return false;
		}
	}

	/** Reafirma el objetivo de ataque cada tick. */
	private static final class Attack extends Directive {
		private final LivingEntity target;

		Attack(Mob mob, LivingEntity target) {
			super(mob);
			this.target = target;
		}

		@Override
		boolean tick() {
			if (target.isRemoved() || !target.isAlive()) {
				mob.setTarget(null);
				return true;
			}
			mob.setTarget(target);
			return false;
		}
	}

	/**
	 * Interpola la posicion del mob de forma cinematica. En modo GROUND toma la altura
	 * real del terreno en cada columna (heightmap) para que el mob quede ENCIMA de los
	 * bloques y no se asfixie; en modo AIR usa la Y literal. En ambos casos orienta al
	 * mob hacia la direccion de avance y le asigna una velocidad coherente.
	 */
	private static final class Glide extends Directive {
		private final Vec3 from;
		private final Vec3 to;
		private final int total;
		private final GlideMode mode;
		private final boolean previousNoGravity;
		private final boolean previousNoAi;
		private int elapsed = 0;

		Glide(Mob mob, Vec3 to, int ticks, GlideMode mode) {
			super(mob);
			this.from = mob.position();
			this.to = to;
			this.total = Math.max(1, ticks);
			this.mode = mode;
			this.previousNoGravity = mob.isNoGravity();
			this.previousNoAi = mob.isNoAi();
			mob.getNavigation().stop();
			mob.setNoGravity(true);
			// Desactiva su IA (paseo/mirar aleatorio) para que no pelee con el movimiento.
			mob.setNoAi(true);
		}

		@Override
		boolean tick() {
			elapsed++;
			double t = Math.min(1.0, (double) elapsed / total);

			double x = from.x + (to.x - from.x) * t;
			double z = from.z + (to.z - from.z) * t;
			double y = groundAwareY(x, z, t);

			// Orientacion segun la direccion CONSTANTE del trayecto (to - from): en linea
			// recta el yaw no cambia, asi que no hay tambaleo.
			faceMovement(mob, to.x - from.x, to.z - from.z, mode == GlideMode.AIR ? (to.y - from.y) : 0.0);

			mob.setPos(x, y, z);
			// Sin velocidad: el cliente no extrapola y la fisica no lo desvia en diagonal.
			mob.setDeltaMovement(Vec3.ZERO);

			if (t >= 1.0) {
				onClear();
				return true;
			}
			return false;
		}

		/** Devuelve la Y de destino: encima del terreno (GROUND) o la interpolada (AIR). */
		private double groundAwareY(double x, double z, double t) {
			if (mode == GlideMode.AIR) {
				return from.y + (to.y - from.y) * t;
			}
			int bx = (int) Math.floor(x);
			int bz = (int) Math.floor(z);
			int surface = mob.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
			// Si la columna no esta cargada (o esta al fondo del mundo), usamos la Y interpolada.
			if (surface <= mob.level().getMinBuildHeight()) {
				return from.y + (to.y - from.y) * t;
			}
			return surface;
		}

		@Override
		void onClear() {
			mob.setNoGravity(previousNoGravity);
			mob.setNoAi(previousNoAi);
			mob.setDeltaMovement(Vec3.ZERO);
		}
	}
}
