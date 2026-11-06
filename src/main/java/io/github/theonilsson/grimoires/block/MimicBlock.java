package io.github.theonilsson.grimoires.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.text.MutableText;

public class MimicBlock extends Block implements Mimic {

	private final BlockState mimicked;

	public MimicBlock(Settings settings, BlockState mimicked) {
		super(settings);
		this.mimicked = mimicked;
	}

	@Override
	public BlockState getMimickedState() {
		return mimicked;
	}

	@Override
	public MutableText getName() {
		return mimicked.getBlock().getName();
	}
}
