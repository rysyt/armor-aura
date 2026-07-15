package com.rysyt.armoraura;

import java.util.List;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ArmorAuraAttachments {

	// Synced because AbstractPiglinMixin reads it client side to drive the shake renderer.
	public static final AttachmentType<SnoutTier> SNOUT_TIER = AttachmentRegistry.<SnoutTier>builder()
			.persistent(SnoutTier.CODEC)
			.syncWith(ByteBufCodecs.fromCodec(SnoutTier.CODEC), AttachmentSyncPredicate.all())
			.buildAndRegister(Identifier.fromNamespaceAndPath(ArmorAura.MOD_ID, "snout_tier"));

	// Pre-rolled super duper lucky drops. Persistent so an unload mid-levitation can't eat the reward.
	// ItemStack.CODEC rejects empty stacks; everything stored here is non-empty by construction.
	public static final AttachmentType<List<ItemStack>> SUPER_LUCKY_DROPS = AttachmentRegistry
			.<List<ItemStack>>builder()
			.persistent(ItemStack.CODEC.listOf())
			.buildAndRegister(Identifier.fromNamespaceAndPath(ArmorAura.MOD_ID, "super_lucky_drops"));

	public static void init() {
		// Static init above does the real work; this just forces it before any world loads.
	}
}
