package io.github.theonilsson.grimoires.spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class SpellElement {

	private final float costMultiplier;

	protected SpellElement(float costMultiplier) {
		this.costMultiplier = costMultiplier;
	}

	public float costMultiplier() {
		return costMultiplier;
	}

	public void onHitEntity(CastContext ctx, LivingEntity target, float power) {}

	public void onHitBlock(CastContext ctx, BlockPos pos, Direction side) {}

	public DamageSource damageSource(CastContext ctx) {
		return DamageSource.magic(ctx.caster(), ctx.caster());
	}
}
