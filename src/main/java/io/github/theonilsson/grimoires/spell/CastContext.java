package io.github.theonilsson.grimoires.spell;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public record CastContext(ServerWorld world, ServerPlayerEntity caster, SpellInstance spell, StaffStats staff) {
	public float param(SpellParam p) {
		return spell.get(p);
	}

	public SpellElement element() {
		return spell.element();
	}
}
