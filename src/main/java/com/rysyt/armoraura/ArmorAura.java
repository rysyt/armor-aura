package com.rysyt.armoraura;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArmorAura implements ModInitializer {
	public static final String MOD_ID = "armor_aura";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Register our tick handler — Fabric calls this 20x/second after each server tick.
		ServerTickEvents.END_SERVER_TICK.register(ArmorAura::onServerTick);
	}

	private static void onServerTick(MinecraftServer server) {
		// Check every online player for active armor trims and apply effects.
		for (var player : server.getPlayerList().getPlayers()) {
			if (TrimChecker.checkTrim(player, "snout")) {
				LOGGER.info("{} is wearing the snout trim!", player.getName().getString());
			}
		}
	}
}