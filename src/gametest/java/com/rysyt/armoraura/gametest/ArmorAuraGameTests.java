package com.rysyt.armoraura.gametest;

import com.rysyt.armoraura.ArmorAuraAttachments;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
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
		ServerPlayer player = spawnPlayer(helper, TrimPatterns.SNOUT);
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
		spawnPlayer(helper, null);
		Piglin piglin = spawnPiglin(helper);
		helper.runAfterDelay(100, () -> {
			helper.assertTrue(
					piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD),
					"piglin ignored a player with no gold and no snout trim");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 400)
	public void goldPickupSetsTierAttachment(GameTestHelper helper) {
		ServerPlayer player = spawnPlayer(helper, TrimPatterns.SNOUT);
		Piglin piglin = spawnPiglin(helper);
		throwGold(helper, player);
		helper.succeedWhen(() -> helper.assertTrue(
				piglin.hasAttached(ArmorAuraAttachments.SNOUT_TIER),
				"piglin picked up a snout trim wearer's gold but got no tier attachment"));
	}

	@GameTest(maxTicks = 600)
	public void tierAttachmentClearsWhenTradeResolves(GameTestHelper helper) {
		ServerPlayer player = spawnPlayer(helper, TrimPatterns.SNOUT);
		Piglin piglin = spawnPiglin(helper);
		throwGold(helper, player);
		boolean[] sawTier = new boolean[1];
		helper.onEachTick(() -> {
			if (piglin.hasAttached(ArmorAuraAttachments.SNOUT_TIER)) {
				sawTier[0] = true;
			}
		});
		// Every tier ends with the attachment consumed: barter throw for lucky and extra lucky,
		// death drop for super duper lucky.
		helper.succeedWhen(() -> {
			helper.assertTrue(sawTier[0], "piglin never picked up the gold");
			helper.assertFalse(piglin.hasAttached(ArmorAuraAttachments.SNOUT_TIER),
					"tier attachment not cleared after the trade resolved");
		});
	}

	@GameTest(maxTicks = 300)
	public void eyeTrimBlocksEndermanStareAggro(GameTestHelper helper) {
		ServerPlayer player = spawnPlayer(helper, TrimPatterns.EYE);
		EnderMan enderman = spawnEnderman(helper);
		stareAt(helper, player, enderman);
		helper.runAfterDelay(150, () -> {
			helper.assertFalse(enderman.hasBeenStaredAt(),
					"enderman registered a stare from an eye trim wearer");
			helper.assertTrue(enderman.getTarget() == null,
					"enderman targeted an eye trim wearer for staring");
			helper.succeed();
		});
	}

	// Control: the same stare without the trim must anger the enderman.
	@GameTest(maxTicks = 300)
	public void endermanAngersAtStareWithoutTrim(GameTestHelper helper) {
		ServerPlayer player = spawnPlayer(helper, null);
		EnderMan enderman = spawnEnderman(helper);
		stareAt(helper, player, enderman);
		boolean[] angered = new boolean[1];
		helper.onEachTick(() -> {
			if (enderman.getTarget() == player || enderman.hasBeenStaredAt()) {
				angered[0] = true;
			}
		});
		helper.succeedWhen(() -> helper.assertTrue(angered[0],
				"enderman never angered at a stare from a player with no trim"));
	}

	// Points the mock player's view at the enderman's eyes every tick. The vanilla stare
	// check needs the view vector within a fraction of a degree, so aim is recomputed as
	// the enderman moves.
	private static void stareAt(GameTestHelper helper, ServerPlayer player, EnderMan enderman) {
		helper.onEachTick(() -> {
			Vec3 toEyes = enderman.getEyePosition().subtract(player.getEyePosition());
			double horizontal = Math.sqrt(toEyes.x * toEyes.x + toEyes.z * toEyes.z);
			float yaw = (float) Math.toDegrees(Math.atan2(-toEyes.x, toEyes.z));
			float pitch = (float) -Math.toDegrees(Math.atan2(toEyes.y, horizontal));
			player.snapTo(player.getX(), player.getY(), player.getZ(), yaw, pitch);
			// The view vector reads head rotation, which snapTo does not touch.
			player.setYHeadRot(yaw);
		});
	}

	private static EnderMan spawnEnderman(GameTestHelper helper) {
		return helper.spawn(EntityType.ENDERMAN, new BlockPos(5, 1, 5));
	}

	// Drops a gold ingot at the piglin's feet, attributed to the thrower like a real Q drop.
	private static void throwGold(GameTestHelper helper, ServerPlayer thrower) {
		ItemEntity gold = helper.spawnItem(Items.GOLD_INGOT, new Vec3(4.5, 1.0, 4.5));
		gold.setThrower(thrower);
	}

	private static ServerPlayer spawnPlayer(GameTestHelper helper, ResourceKey<TrimPattern> trim) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		// The test server world defaults to creative, which mobs never target.
		player.setGameMode(GameType.SURVIVAL);
		Vec3 pos = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
		player.snapTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
		if (trim != null) {
			player.setItemSlot(EquipmentSlot.CHEST, trimmedChestplate(helper, trim));
		}
		return player;
	}

	private static Piglin spawnPiglin(GameTestHelper helper) {
		Piglin piglin = helper.spawn(EntityType.PIGLIN, new BlockPos(5, 1, 5));
		// The test world is the overworld; without this the piglin starts zombifying mid-test.
		piglin.setImmuneToZombification(true);
		return piglin;
	}

	private static ItemStack trimmedChestplate(GameTestHelper helper, ResourceKey<TrimPattern> trim) {
		Holder<TrimPattern> pattern = helper.getLevel().registryAccess()
				.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(trim);
		Holder<TrimMaterial> material = helper.getLevel().registryAccess()
				.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.REDSTONE);
		ItemStack chestplate = new ItemStack(Items.IRON_CHESTPLATE);
		chestplate.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
		return chestplate;
	}
}
