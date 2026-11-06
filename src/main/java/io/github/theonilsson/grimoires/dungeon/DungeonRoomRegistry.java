package io.github.theonilsson.grimoires.dungeon;

import io.github.theonilsson.grimoires.dungeon.room.DungeonRoom;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public final class DungeonRoomRegistry {

	@FunctionalInterface
	public interface Factory {
		DungeonRoom create(BlockPos min, BlockPos max);
	}

	public record Entry(Identifier type, Identifier structureId, Factory factory) {}

	private static final Map<Identifier, Entry> BY_STRUCTURE = new LinkedHashMap<>();
	private static final Map<Identifier, List<Entry>> BY_TYPE = new HashMap<>();

	public static void register(Identifier type, Identifier structureId, Factory factory) {
		Entry entry = new Entry(type, structureId, factory);
		BY_STRUCTURE.put(structureId, entry);
		BY_TYPE.computeIfAbsent(type, t -> new ArrayList<>()).add(entry);
	}

	public static Entry get(Identifier structureId) {
		return BY_STRUCTURE.get(structureId);
	}

	public static List<Entry> getByType(Identifier type) {
		return BY_TYPE.getOrDefault(type, List.of());
	}

	/** Used when loading saved dungeons. Returns null if the id isn't registered. */
	public static DungeonRoom create(Identifier structureId, BlockPos min, BlockPos max) {
		Entry entry = BY_STRUCTURE.get(structureId);
		return entry == null ? null : entry.factory().create(min, max);
	}
}
