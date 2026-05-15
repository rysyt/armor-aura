package com.rysyt.armoraura;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ArmorAura implements ModInitializer {
	public static final String MOD_ID = "armor_aura";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Three-tier barter sets — bridge between pickUpItem() and getBarterResponseItems()
	public static final Set<UUID> SNOUT_LUCKY_PIGLINS = new HashSet<>();        // 75%
	public static final Set<UUID> SNOUT_EXTRA_LUCKY_PIGLINS = new HashSet<>();  // 24%
	public static final Set<UUID> SNOUT_SUPER_LUCKY_PIGLINS = new HashSet<>();  // 1%

	@Override
	public void onInitialize() {
		// Register our tick handler — Fabric calls this 20x/second after each server tick.
		ServerTickEvents.END_SERVER_TICK.register(ArmorAura::onServerTick);
	}

	private static void onServerTick(MinecraftServer server) {
		for (var player : server.getPlayerList().getPlayers()) {
			if (TrimChecker.checkTrim(player, "snout")) {
				LOGGER.info("{} is wearing the snout trim!", player.getName().getString());
			}
		}
	}
}
