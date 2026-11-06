package io.github.theonilsson.grimoires.dungeon.room;

import io.github.theonilsson.grimoires.util.WeightedTable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

public abstract class CombatRoom extends DungeonRoom {

	private static final int MAX_ATTEMPTS_PER_MOB = 40;
	private static final double MIN_PLAYER_DISTANCE_SQ = 6 * 6;

	private final Random rng = new Random();
	private final Set<UUID> spawned = new HashSet<>();

	public CombatRoom(Identifier id, Identifier type, BlockPos min, BlockPos max) {
		super(id, type, min, max);
	}

	/** Tiers of enemies, keyed by cost (e.g. zombie = 1, vindicator = 5, ravager = 15). */
	public abstract WeightedTable<EntityType<? extends MobEntity>> getEnemies();

	protected int getMaxEnemies(ServerWorld world, ServerPlayerEntity player) {
		return 6;
	}

	/**
	 * Maximum total cost for the enemies rolled in this encounter. Calculate it by adding the costs
	 * of the enemies you want (for example, four cost-1 enemies and two cost-5 enemies need a
	 * budget of 14). This is separate from {@link #getMaxEnemies}: the budget limits total enemy
	 * cost, while that setting caps the number of enemies. The roll may spend less than the budget.
	 */
	protected int getEnemyBudget(ServerWorld world, ServerPlayerEntity player) {
		return 20;
	}

	/**
	 * Spread factor for enemy placement. A higher value results in enemies being placed more spread out.
	 */
	protected float getSpread() {
		return 2f;
	}

	@Override
	public void onFirstEntry(ServerWorld world, ServerPlayerEntity player) {
		List<EntityType<? extends MobEntity>> enemies = getEnemies().roll(
			getMaxEnemies(world, player),
			getEnemyBudget(world, player),
			getSpread(),
			rng
		);

		for (EntityType<? extends MobEntity> type : enemies) {
			spawnOne(world, player, type);
		}
	}

	private void spawnOne(ServerWorld world, ServerPlayerEntity player, EntityType<? extends MobEntity> type) {
		MobEntity mob = type.create(world);
		if (mob == null) return;

		for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_MOB; attempt++) {
			BlockPos pos = findStandingSpot(world);
			if (pos == null) continue;

			double x = pos.getX() + 0.5;
			double z = pos.getZ() + 0.5;
			if (player.squaredDistanceTo(x, pos.getY(), z) < MIN_PLAYER_DISTANCE_SQ) continue;

			mob.refreshPositionAndAngles(x, pos.getY(), z, rng.nextFloat() * 360f, 0f);

			if (!world.isSpaceEmpty(mob) || world.containsFluid(mob.getBoundingBox())) continue;

			mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null, null);
			mob.setPersistent();
			world.spawnEntityAndPassengers(mob);
			spawned.add(mob.getUuid());
			return;
		}
	}

	private BlockPos findStandingSpot(ServerWorld world) {
		BlockPos min = getMin();
		BlockPos max = getMax();
		int x = min.getX() + rng.nextInt(max.getX() - min.getX() + 1);
		int z = min.getZ() + rng.nextInt(max.getZ() - min.getZ() + 1);

		List<BlockPos> candidates = new ArrayList<>();
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int y = min.getY() + 1; y <= max.getY(); y++) {
			p.set(x, y, z);
			BlockPos below = p.down();
			boolean solidFloor = world.getBlockState(below)
				.isSideSolidFullSquare(world, below, Direction.UP);
			boolean headroom = world.getBlockState(p).getCollisionShape(world, p).isEmpty()
				&& world.getBlockState(p.up()).getCollisionShape(world, p.up()).isEmpty();
			if (solidFloor && headroom) candidates.add(p.toImmutable());
		}
		return candidates.isEmpty() ? null : candidates.get(rng.nextInt(candidates.size()));
	}

	public Set<UUID> getSpawnedEnemies() {
		return spawned;
	}
}
