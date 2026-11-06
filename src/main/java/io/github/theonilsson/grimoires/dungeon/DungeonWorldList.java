package io.github.theonilsson.grimoires.dungeon;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

public class DungeonWorldList extends PersistentState {

	private final List<Identifier> ids = new ArrayList<>();

	public static DungeonWorldList get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(
			DungeonWorldList::read, DungeonWorldList::new, "grimoires_dungeon_worlds"
		);
	}

	private static DungeonWorldList read(NbtCompound nbt) {
		DungeonWorldList list = new DungeonWorldList();
		NbtList tags = nbt.getList("worlds", NbtElement.STRING_TYPE);
		for (int i = 0; i < tags.size(); i++) {
			list.ids.add(new Identifier(tags.getString(i)));
		}
		return list;
	}

	public List<Identifier> getIds() { return ids; }

	public void add(Identifier id) {
		ids.add(id);
		markDirty();
	}

	public void remove(Identifier id) {
		ids.remove(id);
		markDirty();
	}

	public void removeAll() {
		ids.clear();
		markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		NbtList tags = new NbtList();
		for (Identifier id : ids) tags.add(NbtString.of(id.toString()));
		nbt.put("worlds", tags);
		return nbt;
	}
}
