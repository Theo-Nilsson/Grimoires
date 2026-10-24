package io.github.theonilsson.grimoires.spell;

import io.github.theonilsson.grimoires.item.relics.MajaciteCrownItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

/**
 * THE one place where a spell is allowed to affect an entity.
 * Archetypes must never call target.damage(), setOnFire(), addVelocity() etc. directly:
 * go through here so protections (Majacite, future resistances) apply everywhere.
 */
public final class SpellEffects {

	/** Fraction of a spell that gets through, indexed by the source's rarity tier (0 = Hearthworn ... 5 = Legendary). */
	private static final float[] PASS_BY_TIER = {0f, 0f, 0f, 0f, 0.25f, 1f};

	private SpellEffects() {}

	/** 0 = fully negated, 1 = unaffected. */
	public static float passThrough(CastContext ctx, LivingEntity target) {
		if (!(target instanceof PlayerEntity player) || !MajaciteCrownItem.hasMajaciteCrownEquipped(player)) return 1f;
		int tier = Math.max(0, Math.min(ctx.staff().rarity().getRank(), PASS_BY_TIER.length - 1));
		return PASS_BY_TIER[tier];
	}

	public static boolean canAffect(CastContext ctx, LivingEntity target) {
		return passThrough(ctx, target) > 0f;
	}

	/** Damage + the spell's element hook, both scaled by protection. Returns true if damage was applied. */
	public static boolean damage(CastContext ctx, LivingEntity target, float amount) {
		float pass = passThrough(ctx, target);
		if (pass <= 0f) {
			negated(ctx, target);
			return false;
		}

		float dealt = amount * pass;
		SpellElement element = ctx.element();
		DamageSource source = element != null
			? element.damageSource(ctx)
			: DamageSource.magic(ctx.caster(), ctx.caster());

		boolean applied = target.damage(source, dealt);
		if (applied && element != null) {
			element.onHitEntity(ctx, target, dealt);
		}
		return applied;
	}

	/** For archetypes that touch an entity without damaging it (a barrier burning whatever it shoves). */
	public static void touch(CastContext ctx, LivingEntity target, float power) {
		float pass = passThrough(ctx, target);
		if (pass <= 0f) return;
		SpellElement element = ctx.element();
		if (element != null) element.onHitEntity(ctx, target, power * pass);
	}

	/** Knockback and pushes. */
	public static boolean push(CastContext ctx, LivingEntity target, Vec3d velocity) {
		float pass = passThrough(ctx, target);
		if (pass <= 0f) return false;
		target.addVelocity(velocity.x * pass, velocity.y * pass, velocity.z * pass);
		target.velocityModified = true;
		return true;
	}

	private static void negated(CastContext ctx, LivingEntity target) {
		if (target.age % 10 != 0) return;
		ServerWorld world = ctx.world();
		world.spawnParticles(ParticleTypes.SMOKE, target.getX(), target.getBodyY(0.5), target.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
		world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.PLAYERS, 0.6f, 1.5f);
	}
}
