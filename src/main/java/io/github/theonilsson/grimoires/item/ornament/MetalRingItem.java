package io.github.theonilsson.grimoires.item.ornament;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.component.MagicComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class MetalRingItem extends Item implements StaffOrnament {
	public MetalRingItem(Settings settings) {
		super(settings);
	}

	@Override
	public Identifier getId() {
		return Grimoires.id("metal_ring");
	}

	@Override
	public float onIncomingDamage(PlayerEntity user, DamageSource source, float amount, ItemStack staff) {
		MagicComponent magic = MagicComponent.MAGIC.get(user);
		float cost = amount * 2.0f;
		if (magic.tryConsume(cost)) return 0f;
		return amount;
	}
}
