package com.rysyt.armoraura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ArmorAuraConfigTest {

	@TempDir
	Path tempDir;

	private Path configPath() {
		return tempDir.resolve("armor_aura.json");
	}

	private void assertDefaults(ArmorAuraConfig config) {
		assertEquals(ArmorAuraConfig.DEFAULT_LUCKY_PERCENT, config.luckyPercent);
		assertEquals(ArmorAuraConfig.DEFAULT_EXTRA_LUCKY_PERCENT, config.extraLuckyPercent);
		assertEquals(ArmorAuraConfig.DEFAULT_SUPER_LUCKY_PERCENT, config.superLuckyPercent);
		assertEquals(ArmorAuraConfig.DEFAULT_EXTRA_LUCKY_ADMIRE_TICKS, config.extraLuckyAdmireTicks);
		assertEquals(ArmorAuraConfig.DEFAULT_SUPER_LUCKY_ADMIRE_TICKS, config.superLuckyAdmireTicks);
		assertEquals(ArmorAuraConfig.DEFAULT_SUPER_LUCKY_ROLLS, config.superLuckyRolls);
	}

	@Test
	void missingFileUsesDefaultsAndWritesIt() {
		ArmorAuraConfig config = ArmorAuraConfig.loadFrom(configPath());
		assertDefaults(config);
		assertTrue(Files.exists(configPath()));
	}

	@Test
	void writtenDefaultFileRoundTrips() {
		ArmorAuraConfig.loadFrom(configPath());
		ArmorAuraConfig reloaded = ArmorAuraConfig.loadFrom(configPath());
		assertDefaults(reloaded);
	}

	@Test
	void validFileLoadsAllValues() throws IOException {
		Files.writeString(configPath(), """
			{
				"lucky_percent": 50,
				"extra_lucky_percent": 40,
				"super_lucky_percent": 10,
				"extra_lucky_admire_ticks": 200,
				"super_lucky_admire_ticks": 300,
				"super_lucky_rolls": 10
			}
			""");
		ArmorAuraConfig config = ArmorAuraConfig.loadFrom(configPath());
		assertEquals(50, config.luckyPercent);
		assertEquals(40, config.extraLuckyPercent);
		assertEquals(10, config.superLuckyPercent);
		assertEquals(200, config.extraLuckyAdmireTicks);
		assertEquals(300, config.superLuckyAdmireTicks);
		assertEquals(10, config.superLuckyRolls);
	}

	@Test
	void missingKeysKeepDefaults() throws IOException {
		Files.writeString(configPath(), """
			{ "super_lucky_rolls": 3 }
			""");
		ArmorAuraConfig config = ArmorAuraConfig.loadFrom(configPath());
		assertEquals(3, config.superLuckyRolls);
		assertEquals(ArmorAuraConfig.DEFAULT_LUCKY_PERCENT, config.luckyPercent);
		assertEquals(ArmorAuraConfig.DEFAULT_EXTRA_LUCKY_ADMIRE_TICKS, config.extraLuckyAdmireTicks);
	}

	@Test
	void corruptJsonFallsBackToDefaults() throws IOException {
		Files.writeString(configPath(), "{ not valid json !!!");
		assertDefaults(ArmorAuraConfig.loadFrom(configPath()));
	}

	@Test
	void emptyFileFallsBackToDefaults() throws IOException {
		Files.writeString(configPath(), "");
		assertDefaults(ArmorAuraConfig.loadFrom(configPath()));
	}

	@Test
	void percentsNotSummingTo100ResetToDefaults() throws IOException {
		Files.writeString(configPath(), """
			{ "lucky_percent": 50, "extra_lucky_percent": 40, "super_lucky_percent": 5 }
			""");
		ArmorAuraConfig config = ArmorAuraConfig.loadFrom(configPath());
		assertEquals(ArmorAuraConfig.DEFAULT_LUCKY_PERCENT, config.luckyPercent);
		assertEquals(ArmorAuraConfig.DEFAULT_EXTRA_LUCKY_PERCENT, config.extraLuckyPercent);
		assertEquals(ArmorAuraConfig.DEFAULT_SUPER_LUCKY_PERCENT, config.superLuckyPercent);
	}

	@Test
	void negativePercentResetsToDefaults() throws IOException {
		Files.writeString(configPath(), """
			{ "lucky_percent": 150, "extra_lucky_percent": -51, "super_lucky_percent": 1 }
			""");
		ArmorAuraConfig config = ArmorAuraConfig.loadFrom(configPath());
		assertEquals(ArmorAuraConfig.DEFAULT_EXTRA_LUCKY_PERCENT, config.extraLuckyPercent);
	}

	@Test
	void zeroTicksResetToDefaults() throws IOException {
		Files.writeString(configPath(), """
			{ "extra_lucky_admire_ticks": 0, "super_lucky_admire_ticks": -5 }
			""");
		ArmorAuraConfig config = ArmorAuraConfig.loadFrom(configPath());
		assertEquals(ArmorAuraConfig.DEFAULT_EXTRA_LUCKY_ADMIRE_TICKS, config.extraLuckyAdmireTicks);
		assertEquals(ArmorAuraConfig.DEFAULT_SUPER_LUCKY_ADMIRE_TICKS, config.superLuckyAdmireTicks);
	}

	@Test
	void rollsOutOfRangeResetToDefault() throws IOException {
		Files.writeString(configPath(), """
			{ "super_lucky_rolls": 0 }
			""");
		assertEquals(ArmorAuraConfig.DEFAULT_SUPER_LUCKY_ROLLS,
			ArmorAuraConfig.loadFrom(configPath()).superLuckyRolls);

		Files.writeString(configPath(), """
			{ "super_lucky_rolls": 101 }
			""");
		assertEquals(ArmorAuraConfig.DEFAULT_SUPER_LUCKY_ROLLS,
			ArmorAuraConfig.loadFrom(configPath()).superLuckyRolls);
	}

	@Test
	void invalidValuesDoNotAffectValidOnes() throws IOException {
		Files.writeString(configPath(), """
			{ "super_lucky_rolls": 200, "extra_lucky_admire_ticks": 240 }
			""");
		ArmorAuraConfig config = ArmorAuraConfig.loadFrom(configPath());
		assertEquals(ArmorAuraConfig.DEFAULT_SUPER_LUCKY_ROLLS, config.superLuckyRolls);
		assertEquals(240, config.extraLuckyAdmireTicks);
	}
}
