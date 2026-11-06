package io.github.theonilsson.grimoires.spell.archetype;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import io.github.theonilsson.grimoires.spell.*;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.TeleportTarget;
import org.quiltmc.qsl.worldgen.dimension.api.QuiltDimensions;

import java.util.List;

public class ReturnArchetype extends SpellArchetype {

	public ReturnArchetype() {
		super(Float.MAX_VALUE, 200);
	}

	@Override
	public SpellSchool getSpellSchool(SpellInstance spell) {
		return GrimoiresSpells.PRIMAL;
	}

	@Override
	public List<SpellParam> getParams() {
		return List.of();
	}

	@Override
	public void cast(CastContext ctx) {
		ServerPlayerEntity caster = ctx.caster();
		ServerWorld world = ctx.world();
		MinecraftServer server = world.getServer();
		ServerWorld voidWorld = server.getWorld(Grimoires.VOID_DIMENSION);
		voidWorld.iterateEntities().forEach(entity -> {
			BlockPos spawnPos = world.getSpawnPos();
			QuiltDimensions.teleport(entity,
				world.getRegistryKey() == Grimoires.VOID_DIMENSION ? server.getOverworld() : world,
				new TeleportTarget(
					new Vec3d(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()),
					entity.getVelocity(),
					entity.getYaw(),
					entity.getPitch()
				)
			);
			int rgb = getColor(ctx.spell());
			Vec3f colorVec = new Vec3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
			world.spawnParticles(new DustParticleEffect(colorVec, 1.0f),
				entity.getX(), entity.getY() + entity.getHeight() / 2.0, entity.getZ(),
				30, 0.5, entity.getHeight() / 2.0, 0.5, 0.02);
		});
		world.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
	}
}
