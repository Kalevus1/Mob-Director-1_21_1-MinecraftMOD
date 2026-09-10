package com.mobdirector.neoforge;

import com.mobdirector.MobDirectorCommon;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Arranque para NeoForge: engancha los eventos de NeoForge al núcleo común. */
@Mod(MobDirectorCommon.MOD_ID)
public class MobDirectorNeoForge {

	public MobDirectorNeoForge() {
		MobDirectorCommon.init();
		NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
		NeoForge.EVENT_BUS.addListener(this::onServerTick);
	}

	private void onRegisterCommands(RegisterCommandsEvent event) {
		MobDirectorCommon.registerCommands(event.getDispatcher());
	}

	private void onServerTick(ServerTickEvent.Post event) {
		MobDirectorCommon.onServerTick(event.getServer());
	}
}
