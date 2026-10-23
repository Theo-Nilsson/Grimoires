package io.github.theonilsson.grimoires.component;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;

public record MaxManaModifier(EntityAttributeModifier.Operation operation, float amount) {

	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		int id = operation.getId();
		nbt.putInt("Operation", id);
		nbt.putFloat("ModifierAmount", amount);
		return nbt;
	}

	public static MaxManaModifier fromNbt(final NbtCompound nbt) {
		int id = nbt.getInt("Operation");
		float amount = nbt.getFloat("ModifierAmount");
		return new MaxManaModifier(EntityAttributeModifier.Operation.fromId(id), amount);
	}
}
