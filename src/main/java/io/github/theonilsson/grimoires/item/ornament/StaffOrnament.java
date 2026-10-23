package io.github.theonilsson.grimoires.item.ornament;

import io.github.theonilsson.grimoires.item.StaffPart;
import io.github.theonilsson.grimoires.spell.StaffSlots;
import io.github.theonilsson.grimoires.spell.CastContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface StaffOrnament extends StaffPart {

	default void onTick(PlayerEntity user, ItemStack staff) {}

	default float onIncomingDamage(PlayerEntity user, DamageSource source, float amount, ItemStack staff) {
		return amount;
	}

	default void onCast(CastContext ctx) {}

	@Override
	default String getSlot() {
		return StaffSlots.ORNAMENT;
	}
}
