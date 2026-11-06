package io.github.theonilsson.grimoires.datagen;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.init.GrimoiresBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.model.*;
import net.minecraft.util.Identifier;

public class GrimoiresModelProvider extends FabricModelProvider {

	public GrimoiresModelProvider(FabricDataGenerator dataGenerator) {
		super(dataGenerator);
	}

	@Override
	public void generateBlockStateModels(BlockStateModelGenerator generator) {
		registerMimicBlockState(generator, GrimoiresBlocks.MIMIC_STONE_BRICKS);
		registerMimicBlockState(generator, GrimoiresBlocks.MIMIC_CRACKED_STONE_BRICKS);
		registerMimicBlockState(generator, GrimoiresBlocks.MIMIC_MOSSY_STONE_BRICKS);

		generator.blockStateCollector.accept(
			VariantsBlockStateSupplier.create(GrimoiresBlocks.MIMIC_STONE_BRICK_SLAB)
				.coordinate(
					BlockStateVariantMap.create(
							SlabBlock.TYPE,
							SlabBlock.WATERLOGGED
						)
						.register((type, waterlogged) -> {
							return BlockStateVariant.create()
								.put(VariantSettings.MODEL, mimicModelId(GrimoiresBlocks.MIMIC_STONE_BRICK_SLAB));
						})
				)
		);
		generator.registerParentedItemModel(
			GrimoiresBlocks.MIMIC_STONE_BRICK_SLAB,
			new Identifier("block/stone_brick_slab")
		);
	}

	private static void registerMimicBlockState(BlockStateModelGenerator generator, Block block) {
		generator.blockStateCollector.accept(
			VariantsBlockStateSupplier.create(
				block,
				BlockStateVariant.create().put(VariantSettings.MODEL, mimicModelId(block))
			)
		);
	}

	private static Identifier mimicModelId(Block block) {
		return Grimoires.id("block/" + net.minecraft.util.registry.Registry.BLOCK.getId(block).getPath());
	}

	@Override
	public void generateItemModels(ItemModelGenerator itemModelGenerator) {
	}
}
