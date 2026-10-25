package io.github.theonilsson.grimoires.init;

import io.github.theonilsson.grimoires.Grimoires;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class GrimoiresBlocks {

	public static final Block VOID_BLOCK;

	private static Block register(String name, Block block) {
		Block ret = Registry.register(Registry.BLOCK, Grimoires.id(name), block);
		GrimoiresItems.register(name, new BlockItem(ret, new Item.Settings()));
		return ret;
	}

	public static void init() {}

	static {
		VOID_BLOCK = register("void", new Block(AbstractBlock.Settings.copy(Blocks.BEDROCK).luminance(value -> 15)));
	}
}
