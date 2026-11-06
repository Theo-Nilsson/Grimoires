package io.github.theonilsson.grimoires.dungeon.room;

import io.github.theonilsson.grimoires.init.GrimoiresBlocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public abstract class StartingRoom extends DungeonRoom {

	public StartingRoom(Identifier id, Identifier type, BlockPos min, BlockPos max) {
		super(id, type, min, max);
	}

	public @Nullable BlockPos findSpawnPoint(ServerWorld world) {
		BlockPos min = getMin();
		BlockPos max = getMax();
		for (int x = min.getX(); x <= max.getX(); x++) {
			for (int y = min.getY(); y <= max.getY(); y++) {
				for (int z = min.getZ(); z <= max.getZ(); z++) {
					BlockPos pos = new BlockPos(x, y, z);
					if (world.getBlockState(pos).isOf(GrimoiresBlocks.SPAWN_MARKER_BLOCK)) {
						return pos;
					}
				}
			}
		}
		return null;
	}
}
