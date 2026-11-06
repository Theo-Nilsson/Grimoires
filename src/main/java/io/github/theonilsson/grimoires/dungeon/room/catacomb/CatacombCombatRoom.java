package io.github.theonilsson.grimoires.dungeon.room.catacomb;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.dungeon.room.CombatRoom;
import io.github.theonilsson.grimoires.util.WeightedPool;
import io.github.theonilsson.grimoires.util.WeightedTable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CatacombCombatRoom extends CombatRoom {

	public CatacombCombatRoom(BlockPos min, BlockPos max) {
		super(Grimoires.id("dungeon/0/combat_room_1"), Grimoires.id("catacomb"), min, max);
	}

	@Override
	public WeightedTable<EntityType<? extends MobEntity>> getEnemies() {
		return new WeightedTable<EntityType<? extends MobEntity>>()
			.add(new WeightedPool<EntityType<? extends MobEntity>>()
				.add(EntityType.ZOMBIE, 1)
				.add(EntityType.SKELETON, 1), 1)
			.add(new WeightedPool<EntityType<? extends MobEntity>>()
				.add(EntityType.VINDICATOR, 1)
				.add(EntityType.EVOKER, 1), 5)
			.add(new WeightedPool<EntityType<? extends MobEntity>>()
				.add(EntityType.RAVAGER, 1), 15);
	}

	@Override
	protected int getMaxEnemies(ServerWorld world, ServerPlayerEntity player) {
		return 10;
	}

	@Override
	protected int getEnemyBudget(ServerWorld world, ServerPlayerEntity player) {
		return 35;
	}

	@Override
	protected float getSpread() {
		return 7.5f;
	}

	@Override
	public void tick(ServerWorld world) {
	}

	@Override
	public void onEntered(ServerWorld world, ServerPlayerEntity player) {

	}

	@Override
	public void onExit(ServerWorld world, ServerPlayerEntity player) {

	}
}
