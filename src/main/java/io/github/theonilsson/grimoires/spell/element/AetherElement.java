package io.github.theonilsson.grimoires.spell.element;

import io.github.theonilsson.grimoires.spell.CastContext;
import io.github.theonilsson.grimoires.spell.SpellElement;
import net.minecraft.entity.damage.DamageSource;

public class AetherElement extends SpellElement {

	public AetherElement(float costMultiplier) {
		super(costMultiplier);
	}

	@Override
	public DamageSource damageSource(CastContext ctx) {
		return DamageSource.OUT_OF_WORLD;
	}
}
