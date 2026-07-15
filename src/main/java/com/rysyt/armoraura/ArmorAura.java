package com.rysyt.armoraura;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArmorAura implements ModInitializer {
	public static final String MOD_ID = "armor_aura";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ArmorAuraConfig.load();
		ArmorAuraAttachments.init();
	}
}
