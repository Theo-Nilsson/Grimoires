package io.github.theonilsson.grimoires.spell;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.*;

public class BarrierManager {

	private static final Map<RegistryKey<World>, List<ActiveBarrier>> BARRIERS = new HashMap<>();

	private BarrierManager() {}

	public static void init() {
		ServerTickEvents.END_WORLD_TICK.register(BarrierManager::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> BARRIERS.clear());
	}

	public static void add(ServerWorld world, ActiveBarrier barrier) {
		BARRIERS.computeIfAbsent(world.getRegistryKey(), k -> new ArrayList<>()).add(barrier);
	}

	/**
	 * Call from instant spells (like the ray) after they compute their end point.
	 * Returns a possibly shortened end point, and drains the barrier by `power`.
	 */
	public static Vec3d clipRay(ServerWorld world, Vec3d start, Vec3d end, UUID casterId, float power) {
		List<ActiveBarrier> list = BARRIERS.get(world.getRegistryKey());
		if (list == null) return end;

		ActiveBarrier nearest = null;
		Vec3d nearestPoint = null;
		double best = Double.MAX_VALUE;
		for (ActiveBarrier b : list) {
			if (b.ownerId.equals(casterId)) continue;
			Optional<Vec3d> hit = b.crossing(start, end);
			if (hit.isEmpty()) continue;
			double d = hit.get().squaredDistanceTo(start);
			if (d < best) { best = d; nearest = b; nearestPoint = hit.get(); }
		}
		if (nearest == null) return end;

		nearest.durability -= power;
		impactEffect(world, nearestPoint);
		return nearestPoint;
	}

	private static void tick(ServerWorld world) {
		List<ActiveBarrier> list = BARRIERS.get(world.getRegistryKey());
		if (list == null || list.isEmpty()) return;

		Iterator<ActiveBarrier> it = list.iterator();
		while (it.hasNext()) {
			ActiveBarrier b = it.next();
			b.ticksLeft--;

			if (world.getPlayerByUuid(b.ownerId) == null) { it.remove(); continue; } // owner gone

			blockProjectiles(world, b);
			pushEntities(world, b);

			if (world.getTime() % 4 == 0) drawParticles(world, b);

			if (b.ticksLeft <= 0 || b.durability <= 0) {
				if (b.durability <= 0) breakEffect(world, b);
				it.remove();
			}
		}
	}

	private static void blockProjectiles(ServerWorld world, ActiveBarrier b) {
		Entity owner = world.getPlayerByUuid(b.ownerId);
		for (ProjectileEntity proj : world.getEntitiesByClass(ProjectileEntity.class, b.bounds(), Entity::isAlive)) {
			Entity projOwner = proj.getOwner();
			if (projOwner != null && projOwner.getUuid().equals(b.ownerId)) continue; // own + already-reflected

			Vec3d from = new Vec3d(proj.prevX, proj.prevY, proj.prevZ);
			Vec3d to = proj.getPos();
			Optional<Vec3d> hit = b.crossing(from, to);
			if (hit.isEmpty()) continue;

			Vec3d v = proj.getVelocity();
			Vec3d reflected = v.subtract(b.normal.multiply(2.0 * v.dotProduct(b.normal)));

			proj.setPosition(hit.get().add(reflected.normalize().multiply(0.15)));
			proj.setVelocity(reflected);
			proj.velocityModified = true;
			proj.setOwner(owner);

			if (proj instanceof ExplosiveProjectileEntity fireball) {
				double power = Math.sqrt(fireball.powerX * fireball.powerX + fireball.powerY * fireball.powerY + fireball.powerZ * fireball.powerZ);
				Vec3d dir = reflected.normalize().multiply(power);
				fireball.powerX = dir.x;
				fireball.powerY = dir.y;
				fireball.powerZ = dir.z;
			}

			b.durability -= Math.max(2f, (float) v.length() * 3f);
			impactEffect(world, hit.get());
		}
	}

	private static void pushEntities(ServerWorld world, ActiveBarrier b) {
		for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, b.bounds(),
			en -> en.isAlive() && !en.getUuid().equals(b.ownerId))) {
			Vec3d mid = e.getPos().add(0, e.getHeight() / 2.0, 0);
			double side = b.side(mid);
			double reach = 0.6 + e.getWidth() / 2.0;
			if (Math.abs(side) >= reach || b.planarDistance(mid) > b.radius + e.getWidth() / 2.0) continue;

			double dir = side >= 0 ? 1.0 : -1.0;
			Vec3d push = b.normal.multiply(dir * 0.4);
			SpellEffects.push(b.ctx, e, new Vec3d(push.x, 0.05, push.z));
			SpellEffects.touch(b.ctx, e, 5f);
			b.durability -= 0.5f;
		}
	}

	private static void drawParticles(ServerWorld world, ActiveBarrier b) {
		Vec3f color = new Vec3f(((b.color >> 16) & 255) / 255f, ((b.color >> 8) & 255) / 255f, (b.color & 255) / 255f);
		DustParticleEffect dust = new DustParticleEffect(color, 1.0f);

		int ringPoints = 24;
		for (int i = 0; i < ringPoints; i++) {
			double ang = (Math.PI * 2 * i) / ringPoints;
			Vec3d p = b.center
				.add(b.axisU.multiply(Math.cos(ang) * b.radius))
				.add(b.axisV.multiply(Math.sin(ang) * b.radius));
			world.spawnParticles(dust, p.x, p.y, p.z, 1, 0, 0, 0, 0);
		}

		Random rng = new Random();
		for (int i = 0; i < 10; i++) {
			double ang = rng.nextDouble() * Math.PI * 2;
			double r = Math.sqrt(rng.nextDouble()) * b.radius;
			Vec3d p = b.center
				.add(b.axisU.multiply(Math.cos(ang) * r))
				.add(b.axisV.multiply(Math.sin(ang) * r));
			world.spawnParticles(dust, p.x, p.y, p.z, 1, 0, 0, 0, 0);
		}
	}

	private static void impactEffect(ServerWorld world, Vec3d at) {
		world.spawnParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 6, 0.1, 0.1, 0.1, 0.02);
		world.playSound(null, new BlockPos(at), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.8f, 1.4f);
	}

	private static void breakEffect(ServerWorld world, ActiveBarrier b) {
		world.spawnParticles(ParticleTypes.END_ROD, b.center.x, b.center.y, b.center.z, 40, b.radius * 0.5, b.radius * 0.5, b.radius * 0.5, 0.05);
		world.playSound(null, new BlockPos(b.center), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 0.8f);
	}
}
