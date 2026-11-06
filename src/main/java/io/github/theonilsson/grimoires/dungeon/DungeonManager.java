package io.github.theonilsson.grimoires.dungeon;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.dungeon.room.DungeonRoom;
import io.github.theonilsson.grimoires.dungeon.room.StartingRoom;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionTypes;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DungeonManager {

	private static final Map<RegistryKey<World>, DungeonInstance> INSTANCES = new HashMap<>();

	private static RuntimeWorldConfig createConfig(MinecraftServer server) {
		Registry<Biome> biomes = server.getRegistryManager().get(Registry.BIOME_KEY);
		Holder<Biome> voidBiome = biomes.getHolder(BiomeKeys.THE_VOID).orElseThrow();
		return new RuntimeWorldConfig()
			.setDimensionType(DimensionTypes.OVERWORLD)
			.setGenerator(new VoidChunkGenerator(voidBiome))
			.setDifficulty(Difficulty.HARD)
			.setGameRule(GameRules.DO_DAYLIGHT_CYCLE, false)
			.setGameRule(GameRules.DO_MOB_SPAWNING, false);
	}

	private static RuntimeWorldHandle openWorld(MinecraftServer server, Identifier id) {
		DynamicRegistryManager registryManager = server.getRegistryManager();
		Registry<Biome> biomeRegistry = registryManager.get(Registry.BIOME_KEY);
		Optional<Holder<Biome>> holder = biomeRegistry.getHolder(BiomeKeys.THE_VOID);
		if (holder.isEmpty()) return null;
		return Fantasy.get(server).getOrOpenPersistentWorld(id, createConfig(server));
	}

	public static void tick(ServerWorld world) {
		DungeonInstance instance = INSTANCES.get(world.getRegistryKey());
		if (instance != null) {
			instance.tick(world);
			if (instance.consumeDirty()) {
				DungeonState.get(world).markDirty();
			}
		}
	}

	public static void newDungeon(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		Identifier id = Grimoires.id("dungeon_" + UUID.randomUUID());
		RuntimeWorldHandle handle = openWorld(server, id);
		if (handle == null) return;
		ServerWorld world = handle.asWorld();

		DungeonInstance instance = DungeonGenerator.generateDungeon(world);
		if (instance == null) {
			handle.delete();
			return;
		}

		DungeonWorldList.get(server).add(id);
		DungeonState.get(world).setInstance(instance);
		INSTANCES.put(world.getRegistryKey(), instance);

		DungeonRoom firstRoom = instance.rooms.get(0);
		if (firstRoom instanceof StartingRoom) {
			BlockPos spawn = ((StartingRoom) firstRoom).findSpawnPoint(world);
			if (spawn != null) {
				player.teleport(world, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYaw(), player.getPitch());
			}
		} else {
			player.teleport(world, 0.5, 6, 0.5, player.getYaw(), player.getPitch());
		}
	}

	public static void removeAll(MinecraftServer server) {
		DungeonWorldList.get(server).removeAll();
	}

	/** Re-opens every saved dungeon world and loads its rooms. */
	public static void loadAll(MinecraftServer server) {
		for (Identifier id : DungeonWorldList.get(server).getIds()) {
			RuntimeWorldHandle handle = openWorld(server, id);
			if (handle == null) continue;
			ServerWorld world = handle.asWorld();
			INSTANCES.put(world.getRegistryKey(), DungeonState.get(world).getInstance());
		}
	}

	public static void clear() {
		INSTANCES.clear();
	}
}
