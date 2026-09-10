package com.mobdirector.forge;

import com.mobdirector.MobDirectorCommon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Arranque para Forge: engancha los eventos de Forge al núcleo común. */
@Mod(MobDirectorCommon.MOD_ID)
public class MobDirectorForge {

	public MobDirectorForge() {
		MobDirectorCommon.init();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		MobDirectorCommon.registerCommands(event.getDispatcher());
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			MobDirectorCommon.onServerTick(event.getServer());
		}
	}
}
