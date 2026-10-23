package io.github.theonilsson.grimoires.spell;

import io.github.theonilsson.grimoires.component.MagicComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class SpellCaster {

	private SpellCaster() {}

	public static int castTicks(SpellInstance spell, StaffStats staff) {
		SpellArchetype a = spell.archetype();
		if (a == null) return Integer.MAX_VALUE;
		return Math.max(1, Math.round(a.castTicks(spell) / staff.castSpeed()));
	}

	public static float manaCost(SpellInstance spell, StaffStats staff) {
		SpellArchetype a = spell.archetype();
		SpellElement e = spell.element();
		if (a == null) return Float.MAX_VALUE;
		float eMult = e != null ? e.costMultiplier() : 1f;
		return a.manaCost(spell) * staff.manaEfficiency() * eMult;
	}

	/** Call server-side once the charge time has elapsed. */
	public static boolean tryCast(ServerPlayerEntity player, SpellInstance spell, StaffStats staff) {
		SpellInstance s = spell.copy();
		SpellArchetype a = s.archetype();
		if (a == null) return false;

		for (SpellParam p : s.archetype().getParams()) {
			s = s.with(p, staff.modifyParam(s.archetype(), p, s.get(p)));
		}

		if (!player.isCreative()) {
			MagicComponent magic = MagicComponent.MAGIC.get(player);
			if (!magic.tryConsume(manaCost(s, staff))) return false;
		}
		MagicComponent.MAGIC.sync(player);

		a.cast(new CastContext((ServerWorld) player.world, player, s, staff));
		return true;
	}

}
