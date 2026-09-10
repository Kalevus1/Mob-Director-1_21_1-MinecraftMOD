package com.mobdirector;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Núcleo del mod, independiente del loader. Fabric, NeoForge y Forge comparten esta
 * clase (y {@link MobCommands}, {@link Directives}, {@link Scheduler}); cada loader solo
 * añade un pequeño "arranque" que engancha sus eventos y llama a estos métodos.
 */
public final class MobDirectorCommon {

	public static final String MOD_ID = "mobdirector";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private MobDirectorCommon() {
	}

	/** Mensaje de arranque. Lo llama el punto de entrada de cada loader. */
	public static void init() {
		LOGGER.info("Mob Director cargado. Usa /mobctl para controlar mobs.");
	}

	/** Registra el árbol de comandos /mobctl. Se llama desde el evento de cada loader. */
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		MobCommands.register(dispatcher);
	}

	/** Debe llamarse al final de cada tick del servidor (desde el evento de cada loader). */
	public static void onServerTick(MinecraftServer server) {
		Directives.tick();
		Scheduler.tick(server);
	}
}
