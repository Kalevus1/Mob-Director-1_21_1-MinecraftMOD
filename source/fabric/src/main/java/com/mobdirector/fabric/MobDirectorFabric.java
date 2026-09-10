package com.mobdirector.fabric;

import com.mobdirector.MobDirectorCommon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Arranque para Fabric: engancha los eventos de Fabric al núcleo común. */
public class MobDirectorFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		MobDirectorCommon.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				MobDirectorCommon.registerCommands(dispatcher));

		ServerTickEvents.END_SERVER_TICK.register(MobDirectorCommon::onServerTick);
	}
}
