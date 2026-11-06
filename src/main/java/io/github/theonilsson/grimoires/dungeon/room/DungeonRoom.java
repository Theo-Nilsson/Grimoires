package io.github.theonilsson.grimoires.dungeon.room;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public abstract class DungeonRoom {

	private final Identifier structureLocation;
	private final Identifier type;
	private final Box bounds;
	private final BlockPos min;
	private final BlockPos max;
	private boolean visited;

	public DungeonRoom(Identifier id, Identifier type, BlockPos min, BlockPos max) {
		this.structureLocation = id;
		this.type = type;
		this.min = min;
		this.max = max;
		this.bounds = new Box(min.getX(), min.getY(), min.getZ(),
			max.getX() + 1, max.getY() + 1, max.getZ() + 1);
	}

	public BlockPos getMin() { return min; }
	public BlockPos getMax() { return max; }

	public Identifier getStructureLocation() { return structureLocation; }
	public Identifier getType() { return type; }

	public boolean contains(ServerPlayerEntity player) {
		Vec3d playerPos = player.getPos();
		return bounds.contains(playerPos.x, playerPos.y, playerPos.z);
	}

	public boolean isVisited() { return visited; }
	public void setVisited(boolean visited) { this.visited = visited; }

	public abstract void tick(ServerWorld world);
	public void tickRoomWithPlayers(ServerWorld world, List<ServerPlayerEntity> players) { this.tick(world); }
	public abstract void onEntered(ServerWorld world, ServerPlayerEntity player);
	public abstract void onFirstEntry(ServerWorld world, ServerPlayerEntity player);
	public abstract void onExit(ServerWorld world, ServerPlayerEntity player);
}
