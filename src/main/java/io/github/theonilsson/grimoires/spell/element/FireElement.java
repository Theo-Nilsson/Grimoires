package io.github.theonilsson.grimoires.spell.element;

import io.github.theonilsson.grimoires.spell.CastContext;
import io.github.theonilsson.grimoires.spell.SpellElement;
import net.minecraft.entity.LivingEntity;

public class FireElement extends SpellElement {

	public FireElement(float costMultiplier) {
		super(costMultiplier);
	}

	@Override
	public void onHitEntity(CastContext ctx, LivingEntity target, float power) {
		target.setOnFireFor(Math.round(2 + power * 0.3f));
	}
}
