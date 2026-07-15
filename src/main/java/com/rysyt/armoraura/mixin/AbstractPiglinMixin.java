package com.rysyt.armoraura.mixin;

import com.rysyt.armoraura.ArmorAuraAttachments;
import com.rysyt.armoraura.SnoutTier;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractPiglin.class)
public class AbstractPiglinMixin {

	// PiglinRenderer.isShaking() checks state.isConverting, which is populated from isConverting().
	// Returning true here makes the piglin visually wobble for the duration of the super duper lucky trade.
	// The tier attachment is synced, so this works on the client-side entity the renderer reads.
	@Inject(method = "isConverting", at = @At("HEAD"), cancellable = true)
	private void forceShakeDuringSuperLucky(CallbackInfoReturnable<Boolean> cir) {
		AbstractPiglin self = (AbstractPiglin) (Object) this;
		if (self.getAttached(ArmorAuraAttachments.SNOUT_TIER) == SnoutTier.SUPER_LUCKY) {
			cir.setReturnValue(true);
		}
	}
}
