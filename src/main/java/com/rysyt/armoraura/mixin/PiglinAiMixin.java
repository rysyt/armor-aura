package com.rysyt.armoraura.mixin;

import com.rysyt.armoraura.ArmorAura;
import com.rysyt.armoraura.TrimChecker;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(PiglinAi.class)
public class PiglinAiMixin {

    // Piglins won't aggro players wearing the snout trim.
    @Inject(method = "isWearingSafeArmor", at = @At("HEAD"), cancellable = true)
    private static void snoutTrimBypassesAggro(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ServerPlayer player && TrimChecker.checkTrim(player, "snout")) {
            cir.setReturnValue(true);
        }
    }

    // Roll the trade tier at pickup — the only moment the throwing player is reachable.
    @Inject(method = "pickUpItem", at = @At("HEAD"))
    private static void onPickUpItem(ServerLevel level, Piglin piglin, ItemEntity itemEntity, CallbackInfo ci) {
        Entity owner = itemEntity.getOwner();
        if (owner instanceof ServerPlayer player && TrimChecker.checkTrim(player, "snout")) {
            int roll = new Random().nextInt(100);
            if (roll < 1) {
                ArmorAura.SNOUT_SUPER_LUCKY_PIGLINS.add(piglin.getUUID());
                player.sendOverlayMessage(Component.literal("Super Duper Lucky Trade!"));
            } else if (roll < 25) {
                ArmorAura.SNOUT_EXTRA_LUCKY_PIGLINS.add(piglin.getUUID());
                player.sendOverlayMessage(Component.literal("Extra Lucky Trade!"));
            } else {
                ArmorAura.SNOUT_LUCKY_PIGLINS.add(piglin.getUUID());
                player.sendOverlayMessage(Component.literal("Lucky Trade!"));
            }
        }
    }

    // Override admiration duration for extra lucky and super duper lucky tiers.
    // Super duper lucky also triggers the zombie villager conversion shake (entity event 16).
    @Inject(method = "admireGoldItem", at = @At("HEAD"), cancellable = true)
    private static void extendAdmiration(LivingEntity entity, CallbackInfo ci) {
        if (ArmorAura.SNOUT_SUPER_LUCKY_PIGLINS.contains(entity.getUUID())) {
            entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 269L);
            entity.level().broadcastEntityEvent(entity, (byte) 16);
            ci.cancel();
        } else if (ArmorAura.SNOUT_EXTRA_LUCKY_PIGLINS.contains(entity.getUUID())) {
            entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 179L);
            ci.cancel();
        }
        // Lucky tier uses vanilla 119L — no override needed
    }

    // Select the loot table based on tier. Super duper lucky rolls 5 times and kills the piglin.
    @Inject(method = "getBarterResponseItems", at = @At("HEAD"), cancellable = true)
    private static void tieredBarterItems(Piglin piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (ArmorAura.SNOUT_SUPER_LUCKY_PIGLINS.remove(piglin.getUUID())) {
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                items.addAll(rollSnoutTable(piglin));
                items.add(randomRareItem());
            }
            cir.setReturnValue(items);
            // Schedule death for next tick so items are thrown before the piglin dies
            piglin.level().getServer().execute(() -> piglin.kill((ServerLevel) piglin.level()));

        } else if (ArmorAura.SNOUT_EXTRA_LUCKY_PIGLINS.remove(piglin.getUUID())) {
            List<ItemStack> items = new ArrayList<>(rollSnoutTable(piglin));
            items.add(randomRareItem());
            cir.setReturnValue(items);

        } else if (ArmorAura.SNOUT_LUCKY_PIGLINS.remove(piglin.getUUID())) {
            cir.setReturnValue(rollTable(piglin, "piglin_bartering_lucky"));
        }
    }

    private static List<ItemStack> rollSnoutTable(Piglin piglin) {
        return rollTable(piglin, "piglin_bartering_snout");
    }

    private static List<ItemStack> rollTable(Piglin piglin, String tableName) {
        ResourceKey<LootTable> key = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath("armor_aura", tableName)
        );
        LootTable lootTable = piglin.level().getServer().reloadableRegistries().getLootTable(key);
        return lootTable.getRandomItems(
            new LootParams.Builder((ServerLevel) piglin.level())
                .withParameter(LootContextParams.THIS_ENTITY, piglin)
                .create(LootContextParamSets.PIGLIN_BARTER)
        );
    }

    // Returns a random rare item as a guaranteed bonus drop.
    private static ItemStack randomRareItem() {
        Random rng = new Random();
        return switch (rng.nextInt(5)) {
            case 0 -> new ItemStack(Items.ENDER_PEARL, 2 + rng.nextInt(3));
            case 1 -> {
                ItemStack s = new ItemStack(Items.POTION);
                s.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.FIRE_RESISTANCE));
                yield s;
            }
            case 2 -> new ItemStack(Items.DRIED_GHAST, 1);
            case 3 -> new ItemStack(Items.DIAMOND, 1 + rng.nextInt(2));
            default -> new ItemStack(Items.IRON_NUGGET, 10 + rng.nextInt(10));
        };
    }
}
