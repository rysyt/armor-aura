package com.rysyt.armoraura;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

// Loaded once at mod init. Missing keys keep their defaults; invalid values are
// reset to defaults with a warning rather than crashing the game.
public class ArmorAuraConfig {

	public static final int DEFAULT_LUCKY_PERCENT = 75;
	public static final int DEFAULT_EXTRA_LUCKY_PERCENT = 24;
	public static final int DEFAULT_SUPER_LUCKY_PERCENT = 1;
	public static final int DEFAULT_EXTRA_LUCKY_ADMIRE_TICKS = 179;
	public static final int DEFAULT_SUPER_LUCKY_ADMIRE_TICKS = 269;
	public static final int DEFAULT_SUPER_LUCKY_ROLLS = 5;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ArmorAuraConfig instance = new ArmorAuraConfig();

	@SerializedName("lucky_percent")
	public int luckyPercent = DEFAULT_LUCKY_PERCENT;

	@SerializedName("extra_lucky_percent")
	public int extraLuckyPercent = DEFAULT_EXTRA_LUCKY_PERCENT;

	@SerializedName("super_lucky_percent")
	public int superLuckyPercent = DEFAULT_SUPER_LUCKY_PERCENT;

	@SerializedName("extra_lucky_admire_ticks")
	public int extraLuckyAdmireTicks = DEFAULT_EXTRA_LUCKY_ADMIRE_TICKS;

	@SerializedName("super_lucky_admire_ticks")
	public int superLuckyAdmireTicks = DEFAULT_SUPER_LUCKY_ADMIRE_TICKS;

	@SerializedName("super_lucky_rolls")
	public int superLuckyRolls = DEFAULT_SUPER_LUCKY_ROLLS;

	public static ArmorAuraConfig get() {
		return instance;
	}

	public static void load() {
		instance = loadFrom(FabricLoader.getInstance().getConfigDir().resolve("armor_aura.json"));
	}

	static ArmorAuraConfig loadFrom(Path path) {
		if (!Files.exists(path)) {
			ArmorAuraConfig defaults = new ArmorAuraConfig();
			try {
				Files.writeString(path, GSON.toJson(defaults));
			} catch (IOException e) {
				ArmorAura.LOGGER.warn("Could not write default config to {}: {}", path, e.toString());
			}
			return defaults;
		}
		ArmorAuraConfig config;
		try {
			config = GSON.fromJson(Files.readString(path), ArmorAuraConfig.class);
			if (config == null) {
				config = new ArmorAuraConfig();
			}
		} catch (IOException | JsonParseException e) {
			ArmorAura.LOGGER.warn("Could not read {}, using defaults: {}", path, e.toString());
			config = new ArmorAuraConfig();
		}
		config.validate();
		return config;
	}

	void validate() {
		if (luckyPercent < 0 || extraLuckyPercent < 0 || superLuckyPercent < 0
				|| luckyPercent + extraLuckyPercent + superLuckyPercent != 100) {
			ArmorAura.LOGGER.warn(
				"armor_aura.json: tier percents must be non-negative and sum to 100, resetting to defaults");
			luckyPercent = DEFAULT_LUCKY_PERCENT;
			extraLuckyPercent = DEFAULT_EXTRA_LUCKY_PERCENT;
			superLuckyPercent = DEFAULT_SUPER_LUCKY_PERCENT;
		}
		if (extraLuckyAdmireTicks < 1) {
			ArmorAura.LOGGER.warn("armor_aura.json: extra_lucky_admire_ticks must be at least 1, resetting to default");
			extraLuckyAdmireTicks = DEFAULT_EXTRA_LUCKY_ADMIRE_TICKS;
		}
		if (superLuckyAdmireTicks < 1) {
			ArmorAura.LOGGER.warn("armor_aura.json: super_lucky_admire_ticks must be at least 1, resetting to default");
			superLuckyAdmireTicks = DEFAULT_SUPER_LUCKY_ADMIRE_TICKS;
		}
		if (superLuckyRolls < 1 || superLuckyRolls > 100) {
			ArmorAura.LOGGER.warn("armor_aura.json: super_lucky_rolls must be between 1 and 100, resetting to default");
			superLuckyRolls = DEFAULT_SUPER_LUCKY_ROLLS;
		}
	}
}
