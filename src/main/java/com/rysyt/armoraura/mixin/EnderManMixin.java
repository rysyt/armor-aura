package com.rysyt.armoraura.mixin;

import com.rysyt.armoraura.TrimChecker;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public class EnderManMixin {

	// Endermen never register the stare from a player wearing the eye trim, so no
	// aggro and no creepy scream. Provoking them by attacking still works.
	@Inject(method = "isBeingStaredBy", at = @At("HEAD"), cancellable = true)
	private void eyeTrimBypassesStare(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (TrimChecker.checkTrim(player, "eye")) {
			cir.setReturnValue(false);
		}
	}
}
