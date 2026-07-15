package com.rysyt.armoraura;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorAura implements ModInitializer {
	public static final String MOD_ID = "armor_aura";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Three-tier barter sets, the bridge between pickUpItem() and getBarterResponseItems()
	public static final Set<UUID> SNOUT_LUCKY_PIGLINS = ConcurrentHashMap.newKeySet();        // 75%
	public static final Set<UUID> SNOUT_EXTRA_LUCKY_PIGLINS = ConcurrentHashMap.newKeySet();  // 24%
	public static final Set<UUID> SNOUT_SUPER_LUCKY_PIGLINS = ConcurrentHashMap.newKeySet();  // 1%

	// Pre-rolled drops for super duper lucky, stored at admiration start and spawned at the death location
	public static final Map<UUID, List<ItemStack>> SNOUT_SUPER_LUCKY_DROPS = new ConcurrentHashMap<>();

	@Override
	public void onInitialize() {
		// Tier state is normally removed on a completed trade or on death. Also drop it when the
		// entity unloads (despawn, chunk unload) so UUIDs cannot accumulate, and clear everything
		// on server stop.
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> removeTierState(entity.getUUID()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearTierState());
	}

	private static void removeTierState(UUID uuid) {
		SNOUT_LUCKY_PIGLINS.remove(uuid);
		SNOUT_EXTRA_LUCKY_PIGLINS.remove(uuid);
		SNOUT_SUPER_LUCKY_PIGLINS.remove(uuid);
		SNOUT_SUPER_LUCKY_DROPS.remove(uuid);
	}

	private static void clearTierState() {
		SNOUT_LUCKY_PIGLINS.clear();
		SNOUT_EXTRA_LUCKY_PIGLINS.clear();
		SNOUT_SUPER_LUCKY_PIGLINS.clear();
		SNOUT_SUPER_LUCKY_DROPS.clear();
	}
}
