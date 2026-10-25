package io.github.theonilsson.grimoires.spell.archetype;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import io.github.theonilsson.grimoires.spell.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
		ServerWorld world = ctx.world();
		MinecraftServer server = world.getServer();
		ServerWorld voidWorld = server.getWorld(Grimoires.VOID_DIMENSION);
		voidWorld.iterateEntities().forEach(entity -> {
			BlockPos spawnPos = world.getSpawnPos();
			QuiltDimensions.teleport(entity, world, new TeleportTarget(new Vec3d(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()), entity.getVelocity(), entity.getYaw(), entity.getPitch()));
		});
	}
}
