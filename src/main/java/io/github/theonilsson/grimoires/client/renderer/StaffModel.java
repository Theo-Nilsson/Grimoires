package io.github.theonilsson.grimoires.client.renderer;

import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public record StaffModel(String id, String slot, Identifier model, Identifier texture,
						 @Nullable String attachBone,
						 int tint, boolean translucent) {
	public static final int NO_TINT = 0xFFFFFF;

	public StaffModel(String id, String slot, Identifier model, Identifier texture,
					  @Nullable String attachBone, int tint) {
		this(id, slot, model, texture, attachBone, tint, false);
	}
}
