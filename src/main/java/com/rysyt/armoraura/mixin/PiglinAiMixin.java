package com.rysyt.armoraura.mixin;

import com.rysyt.armoraura.ArmorAura;
import com.rysyt.armoraura.TrimChecker;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(PiglinAi.class)
public class PiglinAiMixin {

    // Inject at the start of isWearingSafeArmor so we can return true early.
    // cancellable = true is required to allow short-circuiting the method.
    @Inject(method = "isWearingSafeArmor", at = @At("HEAD"), cancellable = true)
    private static void snoutTrimBypassesAggro(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ServerPlayer player && TrimChecker.checkTrim(player, "snout")) {
            cir.setReturnValue(true);
        }
    }

    // When a piglin picks up gold, check if the thrower has the snout trim and roll for a lucky trade.
    // itemEntity.getOwner() is the only place the throwing player is accessible — after this the item despawns.
    @Inject(method = "pickUpItem", at = @At("HEAD"))
    private static void onPickUpItem(ServerLevel level, Piglin piglin, ItemEntity itemEntity, CallbackInfo ci) {
        Entity owner = itemEntity.getOwner();
        if (owner instanceof ServerPlayer player && TrimChecker.checkTrim(player, "snout")) {
            if (new Random().nextInt(100) < 25) {
                ArmorAura.SNOUT_LUCKY_PIGLINS.add(piglin.getUUID());
            }
        }
    }

    // For lucky piglins, extend admiration from 119 ticks (~6s) to 179 ticks (~9s).
    // We cancel the original so it doesn't overwrite our longer duration.
    @Inject(method = "admireGoldItem", at = @At("HEAD"), cancellable = true)
    private static void extendAdmiration(LivingEntity entity, CallbackInfo ci) {
        if (ArmorAura.SNOUT_LUCKY_PIGLINS.contains(entity.getUUID())) {
            entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 179L);
            ci.cancel();
        }
    }

    // For lucky piglins, replace the vanilla loot table with our boosted snout version.
    // remove() cleans up the set atomically — this is the last point in the barter chain.
    @Inject(method = "getBarterResponseItems", at = @At("HEAD"), cancellable = true)
    private static void luckyBarterItems(Piglin piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (ArmorAura.SNOUT_LUCKY_PIGLINS.remove(piglin.getUUID())) {
            ResourceKey<LootTable> key = ResourceKey.create(
                Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath("armor_aura", "piglin_bartering_snout")
            );
            LootTable lootTable = piglin.level().getServer().reloadableRegistries().getLootTable(key);
            List<ItemStack> items = lootTable.getRandomItems(
                new LootParams.Builder((ServerLevel) piglin.level())
                    .withParameter(LootContextParams.THIS_ENTITY, piglin)
                    .create(LootContextParamSets.PIGLIN_BARTER)
            );
            cir.setReturnValue(items);
        }
    }
}
