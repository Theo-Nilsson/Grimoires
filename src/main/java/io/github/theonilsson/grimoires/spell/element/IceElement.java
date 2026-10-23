package io.github.theonilsson.grimoires.spell.element;

import io.github.theonilsson.grimoires.spell.CastContext;
import io.github.theonilsson.grimoires.spell.SpellElement;
import net.minecraft.entity.LivingEntity;

public class IceElement extends SpellElement {

	public IceElement(float costMultiplier) {
		super(costMultiplier);
	}

	@Override
	public void onHitEntity(CastContext ctx, LivingEntity target, float power) {
		int freezeTicks = Math.round(20f + power * 24f);
		target.setFrozenTicks(Math.max(target.getMinFreezeDamageTicks(), target.getFrozenTicks() + freezeTicks));
	}
}
