package io.github.theonilsson.grimoires.dungeon.room.catacomb;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.dungeon.room.StartingRoom;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CatacombStartingRoom extends StartingRoom {

	public CatacombStartingRoom(BlockPos min, BlockPos max) {
		super(Grimoires.id("dungeon/0/starting_room"), Grimoires.id("catacomb"), min, max);
	}

	@Override
	public void tick(ServerWorld world) {
	}

	@Override
	public void onEntered(ServerWorld world, ServerPlayerEntity player) {
	}

	@Override
	public void onFirstEntry(ServerWorld world, ServerPlayerEntity player) {
	}

	@Override
	public void onExit(ServerWorld world, ServerPlayerEntity player) {
	}
}
