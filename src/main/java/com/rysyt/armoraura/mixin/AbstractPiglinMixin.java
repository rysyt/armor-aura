package com.rysyt.armoraura.mixin;

import com.rysyt.armoraura.ArmorAura;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractPiglin.class)
public class AbstractPiglinMixin {

	// PiglinRenderer.isShaking() checks state.isConverting, which is populated from isConverting().
	// Returning true here makes the piglin visually wobble for the duration of the super duper lucky trade.
	@Inject(method = "isConverting", at = @At("HEAD"), cancellable = true)
	private void forceShakeDuringSuperLucky(CallbackInfoReturnable<Boolean> cir) {
		AbstractPiglin self = (AbstractPiglin) (Object) this;
		if (ArmorAura.SNOUT_SUPER_LUCKY_PIGLINS.contains(self.getUUID())) {
			cir.setReturnValue(true);
		}
	}
}
