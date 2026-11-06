package io.github.theonilsson.grimoires.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.text.Text;

public class MimicBlockItem extends BlockItem {

	private final BlockState mimic;

	public MimicBlockItem(Block block, Settings settings, BlockState mimic) {
		super(block, settings);
		this.mimic = mimic;
	}

	@Override
	public Text getName() {
		return mimic.getBlock().getName();
	}
}
