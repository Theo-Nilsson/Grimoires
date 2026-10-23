package io.github.theonilsson.grimoires.spell.archetype;

import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import io.github.theonilsson.grimoires.spell.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BarrierArchetype extends SpellArchetype {

	public static final SpellParam RADIUS     = new SpellParam("radius", 1.5f, 6f, 2.5f, 0.8f);
	public static final SpellParam DURATION   = new SpellParam("duration", 3f, 30f, 8f, 0.75f); // seconds
	public static final SpellParam DURABILITY = new SpellParam("durability", 10f, 200f, 40f, 2.5f);
	public static final SpellParam DISTANCE   = new SpellParam("distance", 1.5f, 6f, 3f, 0.1f);

	public BarrierArchetype() {
		super(2f, 7);
	}

	@Override
	public SpellSchool getSpellSchool(SpellInstance spell) {
		return GrimoiresSpells.DEFENSIVE;
	}

	@Override
	public List<SpellParam> getParams() {
		return List.of(RADIUS, DURATION, DURABILITY, DISTANCE);
	}

	@Override
	public void cast(CastContext ctx) {
		ServerWorld world = ctx.world();
		ServerPlayerEntity caster = ctx.caster();

		Vec3d look = caster.getRotationVec(1.0f);
		Vec3d flat = new Vec3d(look.x, 0, look.z);
		Vec3d normal = flat.lengthSquared() < 1.0e-4 ? look : flat.normalize();

		Vec3d center = new Vec3d(caster.getX(), caster.getY() + 1.2, caster.getZ())
			.add(normal.multiply(ctx.param(DISTANCE)));

		BarrierManager.add(world, new ActiveBarrier(
			caster.getUuid(),
			center,
			normal,
			ctx.param(RADIUS),
			Math.round(ctx.param(DURATION) * 20f),
			ctx.param(DURABILITY),
			getColor(ctx.spell()),
			ctx));

		world.playSound(null, caster.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE,
			SoundCategory.PLAYERS, 0.7f, 1.6f);
	}
}
