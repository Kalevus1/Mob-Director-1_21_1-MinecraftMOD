package com.mobdirector;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Cola de comandos programados para ejecutarse tras un retardo. Pensado para grabar:
 * lanzas el comando, te da segundos para colocar la camara y luego se dispara solo,
 * con una cuenta atras en la barra de accion (3... 2... 1...).
 *
 * {@link MobDirector} llama a {@link #tick(MinecraftServer)} en cada END_SERVER_TICK.
 */
public final class Scheduler {

	private static final List<Task> TASKS = new CopyOnWriteArrayList<>();

	private Scheduler() {
	}

	/** Programa un comando (sin la barra inicial) para ejecutarse tras {@code delayTicks} ticks. */
	public static void schedule(CommandSourceStack source, String command, int delayTicks) {
		TASKS.add(new Task(source, command, delayTicks));
	}

	public static void tick(MinecraftServer server) {
		if (TASKS.isEmpty()) {
			return;
		}
		Iterator<Task> it = TASKS.iterator();
		while (it.hasNext()) {
			Task task = it.next();
			if (task.tick(server)) {
				TASKS.remove(task);
			}
		}
	}

	private static final class Task {
		private final CommandSourceStack source;
		private final String command;
		private int remaining;
		private int lastSecondShown = -1;

		Task(CommandSourceStack source, String command, int delayTicks) {
			this.source = source;
			this.command = command;
			this.remaining = delayTicks;
		}

		/** @return true cuando la tarea ya se ejecuto y debe eliminarse. */
		boolean tick(MinecraftServer server) {
			if (remaining <= 0) {
				server.getCommands().performPrefixedCommand(source, command);
				return true;
			}

			int seconds = (remaining + 19) / 20; // segundos restantes redondeando hacia arriba
			if (seconds != lastSecondShown) {
				lastSecondShown = seconds;
				showCountdown(seconds);
			}
			remaining--;
			return false;
		}

		private void showCountdown(int seconds) {
			Component message = Component.literal("Grabando en " + seconds + "...");
			ServerPlayer player = source.getPlayer();
			if (player != null) {
				player.displayClientMessage(message, true); // true = barra de accion
			} else {
				source.sendSystemMessage(message);
			}
		}
	}
}
