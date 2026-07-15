package com.rysyt.armoraura;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

// Barter upgrade tier, rolled when a piglin picks up gold thrown by a snout trim wearer.
public enum SnoutTier implements StringRepresentable {
	LUCKY("lucky"),
	EXTRA_LUCKY("extra_lucky"),
	SUPER_LUCKY("super_lucky");

	public static final Codec<SnoutTier> CODEC = StringRepresentable.fromEnum(SnoutTier::values);

	private final String name;

	SnoutTier(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
