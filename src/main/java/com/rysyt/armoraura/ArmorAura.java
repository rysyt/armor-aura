package com.rysyt.armoraura;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ArmorAura implements ModInitializer {
	public static final String MOD_ID = "armor_aura";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Three-tier barter sets, the bridge between pickUpItem() and getBarterResponseItems()
	public static final Set<UUID> SNOUT_LUCKY_PIGLINS = new HashSet<>();        // 75%
	public static final Set<UUID> SNOUT_EXTRA_LUCKY_PIGLINS = new HashSet<>();  // 24%
	public static final Set<UUID> SNOUT_SUPER_LUCKY_PIGLINS = new HashSet<>();  // 1%

	// Pre-rolled drops for super duper lucky, stored at admiration start and spawned at the death location
	public static final Map<UUID, List<ItemStack>> SNOUT_SUPER_LUCKY_DROPS = new HashMap<>();

	@Override
	public void onInitialize() {
	}
}
