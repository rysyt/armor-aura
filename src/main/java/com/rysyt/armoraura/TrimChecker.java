package com.rysyt.armoraura;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public class TrimChecker {

	// Checks all 4 armor slots for a trim matching patternName (e.g. "snout").
	// Trim patterns are stored as data components on the ItemStack; we read the
	// pattern's registry Holder and match it against a minecraft:<patternName> Identifier.
	public static boolean checkTrim(LivingEntity entity, String patternName) {
		Identifier id = Identifier.withDefaultNamespace(patternName);
		for (EquipmentSlot slot : new EquipmentSlot[]{
				EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
		}) {
			ItemStack stack = entity.getItemBySlot(slot);
			ArmorTrim trim = stack.get(DataComponents.TRIM);
			if (trim != null && trim.pattern().is(id)) return true;
		}
		return false;
	}
}
