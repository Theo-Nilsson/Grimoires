package io.github.theonilsson.grimoires.dungeon;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.dungeon.room.DungeonRoom;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public final class DungeonInstance {

	public final List<DungeonRoom> rooms = new ArrayList<>();
	private final Map<UUID, DungeonRoom> playerRoom = new HashMap<>();
	private boolean dirty;

	public boolean consumeDirty() {
		boolean wasDirty = dirty;
		dirty = false;
		return wasDirty;
	}

	public DungeonInstance addRoom(DungeonRoom room) {
		rooms.add(room);
		return this;
	}

	public void tick(ServerWorld world) {
		Map<DungeonRoom, List<ServerPlayerEntity>> playersInRoom = new HashMap<>();

		for (ServerPlayerEntity player : world.getPlayers()) {
			DungeonRoom room = rooms.stream()
				.filter(r -> r.contains(player))
				.findFirst()
				.orElse(null);

			DungeonRoom old = playerRoom.get(player.getUuid());
			if (old != room) {
				if (old != null) old.onExit(world, player);
				if (room != null) {
					if (!room.isVisited()) {
						room.setVisited(true);
						dirty = true;
						room.onFirstEntry(world, player);
					}
					room.onEntered(world, player);
				}
				playerRoom.put(player.getUuid(), room);
			}
			if (room != null) {
				playersInRoom.computeIfAbsent(room, r -> new ArrayList<>()).add(player);
			}
		}

		for (DungeonRoom room : rooms) {
			room.tickRoomWithPlayers(world, playersInRoom.getOrDefault(room, List.of()));
		}
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		NbtList list = new NbtList();
		for (DungeonRoom room : rooms) {
			NbtCompound tag = new NbtCompound();
			tag.putString("id", room.getStructureLocation().toString());
			tag.putString("type", room.getType().toString());
			tag.put("min", NbtHelper.fromBlockPos(room.getMin()));
			tag.put("max", NbtHelper.fromBlockPos(room.getMax()));
			tag.putBoolean("visited", room.isVisited());
			list.add(tag);
		}
		nbt.put("rooms", list);
		return nbt;
	}

	public static DungeonInstance readNbt(NbtCompound nbt) {
		DungeonInstance instance = new DungeonInstance();
		NbtList list = nbt.getList("rooms", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < list.size(); i++) {
			NbtCompound tag = list.getCompound(i);
			Identifier id = new Identifier(tag.getString("id"));
			Identifier type = new Identifier(tag.getString("type"));
			BlockPos min = NbtHelper.toBlockPos(tag.getCompound("min"));
			BlockPos max = NbtHelper.toBlockPos(tag.getCompound("max"));
			DungeonRoom room = DungeonRoomRegistry.create(id, min, max);
			if (room == null) {
				Grimoires.LOGGER.warn("Unknown dungeon room type {}, skipping", id);
				continue;
			}
			room.setVisited(tag.getBoolean("visited"));
			instance.addRoom(room);
		}
		return instance;
	}
}
