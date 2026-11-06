package io.github.theonilsson.grimoires.dungeon;

import io.github.theonilsson.grimoires.Grimoires;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.*;

public class DungeonConnectorUtil {

	public static final class ConnectorRegion {
		public final BlockPos min;
		public final BlockPos max;
		public final int width;
		public final int height;
		public final int depth;
		public final Direction normal;

		public ConnectorRegion(BlockPos min, BlockPos max, Vec3i structureSize) {
			this.min = min;
			this.max = max;
			this.width = max.getX() - min.getX() + 1;
			this.height = max.getY() - min.getY() + 1;
			this.depth = max.getZ() - min.getZ() + 1;
			this.normal = computeNormal(min, max, structureSize);
		}

		private static Direction computeNormal(BlockPos min, BlockPos max, Vec3i size) {
			if (min.getX() == 0 && max.getX() == size.getX() - 1) {
				return Direction.WEST;
			}
			if (min.getX() == 0) {
				return Direction.WEST;
			}
			if (max.getX() == size.getX() - 1) {
				return Direction.EAST;
			}
			if (min.getY() == 0 && max.getY() == size.getY() - 1) {
				return Direction.DOWN;
			}
			if (min.getY() == 0) {
				return Direction.DOWN;
			}
			if (max.getY() == size.getY() - 1) {
				return Direction.UP;
			}
			if (min.getZ() == 0 && max.getZ() == size.getZ() - 1) {
				return Direction.NORTH;
			}
			if (min.getZ() == 0) {
				return Direction.NORTH;
			}
			if (max.getZ() == size.getZ() - 1) {
				return Direction.SOUTH;
			}
			return null;
		}

		public BlockPos center() {
			return new BlockPos(
				(min.getX() + max.getX()) / 2,
				(min.getY() + max.getY()) / 2,
				(min.getZ() + max.getZ()) / 2
			);
		}

		public boolean sameSizeAs(ConnectorRegion other) {
			return this.width == other.width
				&& this.height == other.height
				&& this.depth == other.depth;
		}

		/** Same size, and facing each other. */
		public boolean canConnectTo(ConnectorRegion other) {
			return this.normal != null
				&& other.normal != null
				&& sameSizeAs(other)
				&& this.normal == other.normal.getOpposite();
		}
	}

	public static BlockPos placementFor(BlockPos fromOrigin, ConnectorRegion fromRegion, ConnectorRegion toRegion) {
		return fromOrigin.add(fromRegion.min).subtract(toRegion.min);
	}

	/** Positions (relative to the structure's origin) of every block of the given type. */
	public static Set<BlockPos> collectConnectorBlocks(Structure structure, Block connectorBlock) {
		Set<BlockPos> positions = new HashSet<>();
		for (Structure.StructureBlockInfo info :
			structure.getInfosForBlock(BlockPos.ORIGIN, new StructurePlacementData(), connectorBlock)) {
			positions.add(info.pos.toImmutable());
		}
		return positions;
	}

	public static List<ConnectorRegion> findConnectorRegions(Structure structure, Block connectorBlock) {
		return findConnectorRegions(collectConnectorBlocks(structure, connectorBlock), structure.getSize());
	}

	public static List<ConnectorRegion> findConnectorRegions(Set<BlockPos> allBlocks, Vec3i structureSize) {
		List<ConnectorRegion> result = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();

		for (BlockPos start : allBlocks) {
			if (visited.contains(start)) continue;

			Queue<BlockPos> queue = new ArrayDeque<>();
			List<BlockPos> regionBlocks = new ArrayList<>();
			queue.add(start);
			visited.add(start);

			while (!queue.isEmpty()) {
				BlockPos pos = queue.poll();
				regionBlocks.add(pos);

				for (Direction dir : Direction.values()) {
					BlockPos next = pos.offset(dir);
					if (allBlocks.contains(next) && !visited.contains(next)) {
						visited.add(next);
						queue.add(next);
					}
				}
			}

			int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

			for (BlockPos pos : regionBlocks) {
				minX = Math.min(minX, pos.getX());
				minY = Math.min(minY, pos.getY());
				minZ = Math.min(minZ, pos.getZ());

				maxX = Math.max(maxX, pos.getX());
				maxY = Math.max(maxY, pos.getY());
				maxZ = Math.max(maxZ, pos.getZ());
			}

			// The bounding box must be completely filled, otherwise it's not a rectangular connector
			int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
			if (volume != regionBlocks.size()) {
				Grimoires.LOGGER.warn(
					"Connector region near {} is not a solid box ({} blocks in a box of {}), skipping",
					start, regionBlocks.size(), volume
				);
				continue;
			}

			result.add(new ConnectorRegion(
				new BlockPos(minX, minY, minZ),
				new BlockPos(maxX, maxY, maxZ),
				structureSize
			));
		}

		// HashSet order is arbitrary, so sort to keep generation deterministic
		result.sort(Comparator
			.comparingInt((ConnectorRegion r) -> r.min.getY())
			.thenComparingInt(r -> r.min.getX())
			.thenComparingInt(r -> r.min.getZ()));

		return result;
	}

	/**
	 * Places toStructure so that toRegion lines up exactly with fromRegion.
	 * Returns where it was placed, or null if the regions can't connect.
	 */
	public static BlockPos connectPieces(
		ServerWorld world,
		Structure toStructure,
		BlockPos fromOrigin,
		ConnectorRegion fromRegion,
		ConnectorRegion toRegion
	) {
		if (!fromRegion.canConnectTo(toRegion)) {
			Grimoires.LOGGER.warn("Connectors don't match (size or facing), not placing piece");
			return null;
		}

		BlockPos placeAt = placementFor(fromOrigin, fromRegion, toRegion);

		toStructure.place(
			world,
			placeAt,
			placeAt,
			new StructurePlacementData(),
			world.getRandom(),
			Block.NOTIFY_LISTENERS
		);
		return placeAt;
	}
}
