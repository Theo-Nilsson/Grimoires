package io.github.theonilsson.grimoires.spell.archetype;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import io.github.theonilsson.grimoires.spell.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;
import org.quiltmc.qsl.worldgen.dimension.api.QuiltDimensions;

import java.util.Comparator;
import java.util.List;

public class ErasureArchetype extends SpellArchetype {

	public ErasureArchetype() {
		super(Float.MAX_VALUE, 200);
	}

	@Override
	public SpellSchool getSpellSchool(SpellInstance spell) {
		return GrimoiresSpells.PRIMAL;
	}

	@Override
	public List<SpellParam> getParams() {
		return List.of(RANGE);
	}

	@Override
	public void cast(CastContext ctx) {
		PlayerEntity caster = ctx.caster();
		ServerWorld world = ctx.world();
		double range = ctx.param(RANGE);
		double width = 0.1;
		world.playSound(null, caster.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
			SoundCategory.PLAYERS, 1.0f, 1.0f);

		final Vec3d start = caster.getEyePos();
		Vec3d dir = caster.getRotationVec(1.0f);
		Vec3d end = start.add(dir.multiply(range));

		BlockHitResult blockHit = world.raycast(new RaycastContext(
			start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster));
		if (blockHit.getType() != HitResult.Type.MISS) {
			end = blockHit.getPos();
		}
		final Vec3d stop = end;

		List<Entity> candidates = world.getOtherEntities(
			caster, new Box(start, stop).expand(width + 1.0), e -> e instanceof LivingEntity && e.isAlive());
		candidates.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(start)));

		candidates.stream().findFirst().ifPresent(entity -> {
			if (!(entity instanceof LivingEntity living)) return;
			if (!SpellEffects.canAffect(ctx, living)) return;
			ServerWorld voidWorld = world.getServer().getWorld(Grimoires.VOID_DIMENSION);
			if (voidWorld == null) {
				Grimoires.LOGGER.error("Cannot erase entity: the void dimension is not loaded");
				return;
			}

			int rgb = getColor(ctx.spell());
			Vec3f colorVec = new Vec3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
			world.spawnParticles(new DustParticleEffect(colorVec, 1.0f),
				entity.getX(), entity.getY() + entity.getHeight() / 2.0, entity.getZ(),
				30, 0.5, entity.getHeight() / 2.0, 0.5, 0.02);
			voidWorld.setChunkForced(0, 0, true);
			QuiltDimensions.teleport(entity, voidWorld, new TeleportTarget(new Vec3d(0.5, -61, 0.5), entity.getVelocity(), entity.getYaw(), entity.getPitch()));
		});
	}
}
