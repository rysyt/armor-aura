package com.rysyt.armoraura.mixin;

import com.rysyt.armoraura.TrimChecker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
}
