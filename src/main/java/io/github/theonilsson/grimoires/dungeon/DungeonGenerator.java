package io.github.theonilsson.grimoires.dungeon;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.init.GrimoiresBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.function.IntUnaryOperator;

public class DungeonGenerator {

	private static final Identifier START_ID = Grimoires.id("dungeon/0/starting_room");
	private static final int MAX_ROOMS = 20;
	private static final Block SEAL_BLOCK = GrimoiresBlocks.MIMIC_STONE_BRICKS;

	private record Placement(Structure piece, BlockPos origin, DungeonConnectorUtil.ConnectorRegion used) {}
	private record OpenConnector(BlockPos origin, DungeonConnectorUtil.ConnectorRegion region) {}

	/** Returns the generated dungeon, or null if the starting room couldn't be loaded. */
	public static DungeonInstance generateDungeon(ServerWorld world) {
		StructureTemplateManager templateManager = world.getServer().getStructureTemplateManager();

		DungeonRoomRegistry.Entry startEntry = DungeonRoomRegistry.get(START_ID);
		Optional<Structure> start = templateManager.getStructure(START_ID);
		if (startEntry == null || start.isEmpty()) {
			Grimoires.LOGGER.error("Starting room {} is not registered or its structure is missing", START_ID);
			return null;
		}

		DungeonInstance instance = new DungeonInstance();
		List<Box> occupied = new ArrayList<>();
		Deque<OpenConnector> open = new ArrayDeque<>();
		List<OpenConnector> deadEnds = new ArrayList<>();

		BlockPos startOrigin = BlockPos.ORIGIN;
		start.get().place(world, startOrigin, startOrigin, new StructurePlacementData(),
			world.getRandom(), Block.NOTIFY_LISTENERS);
		instance.addRoom(startEntry.factory().create(startOrigin, maxCorner(startOrigin, start.get())));
		occupied.add(fullBounds(startOrigin, start.get()));

		for (DungeonConnectorUtil.ConnectorRegion region :
			DungeonConnectorUtil.findConnectorRegions(start.get(), GrimoiresBlocks.DUNGEON_CONNECTOR_BLOCK)) {
			open.add(new OpenConnector(startOrigin, region));
		}

		// Every registered room of the starting room's type, except the starting room itself
		List<DungeonRoomRegistry.Entry> candidates = new ArrayList<>(DungeonRoomRegistry.getByType(startEntry.type()));
		Grimoires.LOGGER.info("Candidates for type {}: {}", startEntry.type(),
			candidates.stream().map(e -> e.structureId().toString()).toList());
		candidates.removeIf(e -> e.structureId().equals(START_ID));

		while (!open.isEmpty()) {
			OpenConnector connector = open.poll();

			Placement placed = null;
			if (instance.rooms.size() < MAX_ROOMS) {
				placed = tryAttach(world, templateManager, instance, occupied, candidates, connector);
			}
			if (placed == null) {
				deadEnds.add(connector);
				continue;
			}

			// The new piece's other connectors become open
			for (DungeonConnectorUtil.ConnectorRegion region :
				DungeonConnectorUtil.findConnectorRegions(placed.piece(), GrimoiresBlocks.DUNGEON_CONNECTOR_BLOCK)) {
				if (region.min.equals(placed.used().min)) continue;
				open.add(new OpenConnector(placed.origin(), region));
			}
		}

		for (OpenConnector deadEnd : deadEnds) {
			fill(world, deadEnd, SEAL_BLOCK);
		}

		return instance;
	}

	/** Tries the candidates in random order and places the first one that fits. Returns null if none do. */
	private static Placement tryAttach(
		ServerWorld world,
		StructureTemplateManager templateManager,
		DungeonInstance instance,
		List<Box> occupied,
		List<DungeonRoomRegistry.Entry> candidates,
		OpenConnector connector
	) {
		List<DungeonRoomRegistry.Entry> shuffled = new ArrayList<>(candidates);
		shuffle(shuffled, bound -> world.getRandom().nextInt(bound));

		for (DungeonRoomRegistry.Entry entry : shuffled) {
			Optional<Structure> piece = templateManager.getStructure(entry.structureId());
			if (piece.isEmpty()) {
				Grimoires.LOGGER.error("Couldn't find piece {}", entry.structureId());
				continue;
			}

			for (DungeonConnectorUtil.ConnectorRegion target :
				DungeonConnectorUtil.findConnectorRegions(piece.get(), GrimoiresBlocks.DUNGEON_CONNECTOR_BLOCK)) {
				if (!connector.region().canConnectTo(target)) continue;

				BlockPos placeAt = DungeonConnectorUtil.placementFor(connector.origin(), connector.region(), target);
				if (overlapsAny(placementBounds(placeAt, piece.get(), target), occupied)) {
					Grimoires.LOGGER.info("{} rejected: overlaps an existing room at {}", entry.structureId(), placeAt);
					continue;
				};

				BlockPos placedAt = DungeonConnectorUtil.connectPieces(
					world, piece.get(), connector.origin(), connector.region(), target
				);
				if (placedAt == null) continue;

				fill(world, connector, Blocks.AIR);
				occupied.add(fullBounds(placedAt, piece.get()));
				instance.addRoom(entry.factory().create(placedAt, maxCorner(placedAt, piece.get())));
				return new Placement(piece.get(), placedAt, target);
			}
		}
		return null;
	}

	private static Box placementBounds(BlockPos origin, Structure structure, DungeonConnectorUtil.ConnectorRegion target) {
		Vec3i size = structure.getSize();
		int[] lo = {origin.getX(), origin.getY(), origin.getZ()};
		int[] hi = {lo[0] + size.getX(), lo[1] + size.getY(), lo[2] + size.getZ()}; // exclusive

		int axis = target.normal.getAxis().ordinal();
		int thickness = new int[]{target.width, target.height, target.depth}[axis];
		if (target.normal.getDirection() == Direction.AxisDirection.POSITIVE) {
			hi[axis] -= thickness;
		} else {
			lo[axis] += thickness;
		}
		return new Box(lo[0], lo[1], lo[2], hi[0], hi[1], hi[2]);
	}

	private static Box fullBounds(BlockPos origin, Structure structure) {
		Vec3i size = structure.getSize();
		return new Box(origin.getX(), origin.getY(), origin.getZ(),
			origin.getX() + size.getX(), origin.getY() + size.getY(), origin.getZ() + size.getZ());
	}

	private static boolean overlapsAny(Box box, List<Box> occupied) {
		for (Box other : occupied) {
			if (box.intersects(other)) return true;
		}
		return false;
	}

	private static BlockPos maxCorner(BlockPos origin, Structure structure) {
		Vec3i size = structure.getSize();
		return origin.add(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
	}

	private static void fill(ServerWorld world, OpenConnector connector, Block block) {
		for (BlockPos pos : BlockPos.iterate(
			connector.origin().add(connector.region().min), connector.origin().add(connector.region().max))) {
			world.setBlockState(pos, block.getDefaultState(), Block.NOTIFY_LISTENERS);
		}
	}

	private static <T> void shuffle(List<T> list, IntUnaryOperator nextInt) {
		for (int i = list.size() - 1; i > 0; i--) {
			Collections.swap(list, i, nextInt.applyAsInt(i + 1));
		}
	}
}
