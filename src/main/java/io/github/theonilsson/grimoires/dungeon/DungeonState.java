package io.github.theonilsson.grimoires.dungeon;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class DungeonState extends PersistentState {

	private DungeonInstance instance;

	private DungeonState(DungeonInstance instance) {
		this.instance = instance;
	}

	public static DungeonState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(
			nbt -> new DungeonState(DungeonInstance.readNbt(nbt)),
			() -> new DungeonState(new DungeonInstance()),
			"grimoires_dungeon"
		);
	}

	public DungeonInstance getInstance() { return instance; }

	public void setInstance(DungeonInstance instance) {
		this.instance = instance;
		markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		return instance.writeNbt(nbt);
	}
}
