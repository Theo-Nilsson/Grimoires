package io.github.theonilsson.grimoires.spell.archetype;

import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import io.github.theonilsson.grimoires.spell.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.RaycastContext;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RayArchetype extends SpellArchetype {

	public static final SpellParam WIDTH = new SpellParam("width", 0.1f, 1.0f, 0.25f, 0.5f);

	public RayArchetype() {
		super(6f, 20);
	}

	@Override
	public SpellSchool getSpellSchool(SpellInstance spell) {
		return GrimoiresSpells.OFFENSIVE;
	}

	@Override
	public List<SpellParam> getParams() {
		return List.of(RANGE, WIDTH, PIERCE, DAMAGE);
	}

	@Override
	public void cast(CastContext ctx) {
		ServerWorld world = ctx.world();
		ServerPlayerEntity caster = ctx.caster();

		float range = ctx.param(RANGE);
		float width = ctx.param(WIDTH);
		int maxHits = Math.round(ctx.param(PIERCE)) + 1;
		float damage = ctx.param(DAMAGE) * ctx.staff().spellPower();

		final Vec3d start = caster.getEyePos();
		Vec3d dir = caster.getRotationVec(1.0f);
		Vec3d end = start.add(dir.multiply(range));

		BlockHitResult blockHit = world.raycast(new RaycastContext(
			start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster));
		if (blockHit.getType() != HitResult.Type.MISS) {
			end = blockHit.getPos();
		}
		final Vec3d stop = BarrierManager.clipRay(world, start, end, caster.getUuid(), damage);

		List<Entity> candidates = world.getOtherEntities(
			caster, new Box(start, stop).expand(width + 1.0), e -> e instanceof LivingEntity && e.isAlive());
		candidates.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(start)));

		int hits = 0;
		for (Entity e : candidates) {
			if (hits >= maxHits) break;
			Optional<Vec3d> hit = e.getBoundingBox().expand(width).raycast(start, stop);
			if (hit.isEmpty()) continue;
			DamageSource src = ctx.element() != null ? ctx.element().damageSource(ctx) : DamageSource.magic(caster, caster);
			e.damage(src, damage);
			if (ctx.element() != null && e instanceof LivingEntity living) {
				ctx.element().onHitEntity(ctx, living, damage);
			}
			hits++;
		}

		spawnBeam(world, start, stop, getColor(ctx.spell()));
		world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
			SoundCategory.PLAYERS, 1.0f, 1.2f);
	}

	private static void spawnBeam(ServerWorld world, Vec3d from, Vec3d to, int rgb) {
		Vec3f color = new Vec3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
		DustParticleEffect particle = new DustParticleEffect(color, 1.0f);
		Vec3d step = to.subtract(from);
		double length = step.length();
		Vec3d unit = step.normalize();
		for (double d = 0; d < length; d += 0.5) {
			Vec3d p = from.add(unit.multiply(d));
			world.spawnParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
		}
	}
}
