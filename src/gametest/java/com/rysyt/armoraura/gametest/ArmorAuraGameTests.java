package com.rysyt.armoraura.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class ArmorAuraGameTests {

	@GameTest(maxTicks = 200)
	public void snoutTrimBlocksPiglinTargeting(GameTestHelper helper) {
		ServerPlayer player = spawnPlayer(helper, true);
		Piglin piglin = spawnPiglin(helper);
		helper.runAfterDelay(100, () -> {
			helper.assertFalse(
					piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD),
					"piglin marked a snout trim wearer as targetable");
			helper.assertFalse(player.isRemoved(), "mock player disappeared mid-test");
			helper.succeed();
		});
	}

	// Control: without the trim the same setup must produce aggro, proving the harness detects it.
	@GameTest(maxTicks = 200)
	public void piglinTargetsPlayerWithoutTrim(GameTestHelper helper) {
		spawnPlayer(helper, false);
		Piglin piglin = spawnPiglin(helper);
		helper.runAfterDelay(100, () -> {
			helper.assertTrue(
					piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD),
					"piglin ignored a player with no gold and no snout trim");
			helper.succeed();
		});
	}

	private static ServerPlayer spawnPlayer(GameTestHelper helper, boolean snoutTrim) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		// The test server world defaults to creative, which mobs never target.
		player.setGameMode(GameType.SURVIVAL);
		Vec3 pos = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
		player.snapTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
		if (snoutTrim) {
			player.setItemSlot(EquipmentSlot.CHEST, snoutTrimmedChestplate(helper));
		}
		return player;
	}

	private static Piglin spawnPiglin(GameTestHelper helper) {
		Piglin piglin = helper.spawn(EntityType.PIGLIN, new BlockPos(5, 1, 5));
		// The test world is the overworld; without this the piglin starts zombifying mid-test.
		piglin.setImmuneToZombification(true);
		return piglin;
	}

	private static ItemStack snoutTrimmedChestplate(GameTestHelper helper) {
		Holder<TrimPattern> pattern = helper.getLevel().registryAccess()
				.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.SNOUT);
		Holder<TrimMaterial> material = helper.getLevel().registryAccess()
				.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.REDSTONE);
		ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
		chestplate.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
		return chestplate;
	}
}
