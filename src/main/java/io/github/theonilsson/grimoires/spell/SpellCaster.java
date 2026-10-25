package io.github.theonilsson.grimoires.spell;

import io.github.theonilsson.grimoires.component.MagicComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public final class SpellCaster {

	private SpellCaster() {}

	public static int castTicks(PlayerEntity player, SpellInstance spell, StaffStats staff) {
		SpellArchetype a = spell.archetype();
		if (a == null) return Integer.MAX_VALUE;
		float suppression = MagicSuppression.of(player);
		if (suppression <= 0f) return Integer.MAX_VALUE;
		float ticks = a.castTicks(spell) / staff.castSpeed() / suppression;
		return ticks >= Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, Math.round(ticks));
	}

	public static float manaCost(PlayerEntity player, SpellInstance spell, StaffStats staff) {
		SpellArchetype a = spell.archetype();
		SpellElement e = spell.element();
		if (a == null) return Float.MAX_VALUE;
		float suppression = MagicSuppression.of(player);
		if (suppression <= 0f) return Float.MAX_VALUE;
		float eMult = e != null ? e.costMultiplier() : 1f;
		float cost = a.manaCost(spell) * staff.manaEfficiency() * eMult / suppression;
		return Float.isFinite(cost) ? cost : Float.MAX_VALUE;
	}

	/** Call server-side once the charge time has elapsed. */
	public static boolean tryCast(ServerPlayerEntity player, SpellInstance spell, StaffStats staff) {
		if (MagicSuppression.of(player) <= 0f) {
			player.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE);
			return false;
		}

		SpellInstance s = spell.copy();
		SpellArchetype a = s.archetype();
		if (a == null) return false;

		for (SpellParam p : s.archetype().getParams()) {
			s = s.with(p, staff.modifyParam(s.archetype(), p, s.get(p)));
		}

		if (!player.isCreative()) {
			MagicComponent magic = MagicComponent.MAGIC.get(player);
			if (!magic.tryConsume(manaCost(player, s, staff), true)) return false;
		}
		MagicComponent.MAGIC.sync(player);

		a.cast(new CastContext((ServerWorld) player.world, player, s, staff));
		return true;
	}

}
