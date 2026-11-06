package io.github.theonilsson.grimoires.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;

public class MimicSlabBlock extends SlabBlock implements Mimic {

	public final BlockState mimicked;

	public MimicSlabBlock(Settings settings, BlockState mimicked) {
		super(settings);
		this.mimicked = mimicked;
	}

	@Override
	public BlockState getMimickedState() {
		return mimicked;
	}
}
