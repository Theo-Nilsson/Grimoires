package io.github.theonilsson.grimoires.init;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.block.*;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;

public class GrimoiresBlocks {

	public static final Block DUNGEON_CONNECTOR_BLOCK;
	public static final Block VOID_BLOCK;
	public static final Block SPAWN_MARKER_BLOCK;

	public static final Block MIMIC_STONE_BRICKS;
	public static final Block MIMIC_CRACKED_STONE_BRICKS;
	public static final Block MIMIC_MOSSY_STONE_BRICKS;
	public static final Block MIMIC_STONE_BRICK_SLAB;

	private static Block register(String name, Block block, @Nullable ItemGroup group) {
		registerBlockItem(name, block, group);
		return Registry.register(Registry.BLOCK, Grimoires.id(name), block);
	}

	private static Block register(String name, Block block) {
		return register(name, block, null);
	}

	private static Block registerMimic(String name, Mimic block) {
		if (!(block instanceof Block)) {
			throw new IllegalArgumentException("Mimic " + name + " must be a Block");
		}
		return register(name, (Block) block, GrimoiresItems.DUNGEON_BLOCKS);
	}

	private static Item registerBlockItem(String name, Block block, @Nullable ItemGroup group) {
		Item.Settings settings = new Item.Settings();
		if (group != null) settings.group(group);
		BlockItem item;
		if (block instanceof Mimic mimic) item = new MimicBlockItem(block, settings, mimic.getMimickedState());
		else item = new BlockItem(block, settings);
		return Registry.register(Registry.ITEM, Grimoires.id(name), item);
	}

	public static void init() {}

	static {
		DUNGEON_CONNECTOR_BLOCK = register("dungeon_connector", new DungeonConnectorBlock(AbstractBlock.Settings.copy(Blocks.GLASS)), GrimoiresItems.DUNGEON_BLOCKS);
		VOID_BLOCK = register("void", new Block(AbstractBlock.Settings.copy(Blocks.BEDROCK).luminance(value -> 15)));
		SPAWN_MARKER_BLOCK = register("spawn_marker", new Block(AbstractBlock.Settings.copy(Blocks.BARRIER).noCollision()), GrimoiresItems.DUNGEON_BLOCKS);

		MIMIC_STONE_BRICKS = registerMimic("mimic_stone_bricks", new MimicBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK), Blocks.STONE_BRICKS.getDefaultState()));
		MIMIC_CRACKED_STONE_BRICKS = registerMimic("mimic_cracked_stone_bricks", new MimicBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK), Blocks.CRACKED_STONE_BRICKS.getDefaultState()));
		MIMIC_MOSSY_STONE_BRICKS = registerMimic("mimic_mossy_stone_bricks", new MimicBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK), Blocks.MOSSY_STONE_BRICKS.getDefaultState()));
		MIMIC_STONE_BRICK_SLAB = registerMimic("mimic_stone_brick_slab", new MimicSlabBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK), Blocks.STONE_BRICK_SLAB.getDefaultState()));
	}
}
