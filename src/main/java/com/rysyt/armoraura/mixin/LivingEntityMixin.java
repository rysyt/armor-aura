package com.rysyt.armoraura.mixin;

import com.rysyt.armoraura.ArmorAura;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    // Super duper lucky piglins die the instant they touch the ground.
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void forceDeathOnFall(double fallDistance, float damageModifier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (fallDistance > 0 && ArmorAura.SNOUT_SUPER_LUCKY_DROPS.containsKey(self.getUUID())) {
            if (self.level() instanceof ServerLevel serverLevel) {
                self.kill(serverLevel);
                cir.setReturnValue(true);
            }
        }
    }

    // Spawn pre-rolled drops at the piglin's death position.
    @Inject(method = "die", at = @At("HEAD"))
    private void onDie(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        List<ItemStack> drops = ArmorAura.SNOUT_SUPER_LUCKY_DROPS.remove(self.getUUID());
        ArmorAura.SNOUT_SUPER_LUCKY_PIGLINS.remove(self.getUUID());
        if (drops != null && self.level() instanceof ServerLevel serverLevel) {
            for (ItemStack stack : drops) {
                self.spawnAtLocation(serverLevel, stack);
            }
        }
    }
}
