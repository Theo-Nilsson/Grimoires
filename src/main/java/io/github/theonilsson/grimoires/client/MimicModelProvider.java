package io.github.theonilsson.grimoires.client;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.block.Mimic;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class MimicModelProvider implements ModelVariantProvider {

	private static final String NAMESPACE = Grimoires.MOD_ID;

	@Override
	public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
		if (!modelId.getNamespace().equals(NAMESPACE)) return null;

		String blockPath = modelId.getPath();
		if (blockPath.startsWith("block/")) {
			blockPath = blockPath.substring("block/".length());
		}

		Block block = Registry.BLOCK.get(new Identifier(modelId.getNamespace(), blockPath));
		if (!(block instanceof Mimic mimic)) return null;

		BlockState target = mimic.getMimickedState();

		if (modelId.getVariant().equals("inventory")) {
			Identifier itemId = Registry.ITEM.getId(target.getBlock().asItem());
			return context.loadModel(new ModelIdentifier(itemId, "inventory"));
		}

		for (BlockState state : block.getStateManager().getStates()) {
			if (BlockModels.getModelId(state).getVariant().equals(modelId.getVariant())) {
				return context.loadModel(BlockModels.getModelId(copyProperties(state, target)));
			}
		}
		return null;
	}

	private static BlockState copyProperties(BlockState from, BlockState target) {
		BlockState result = target;
		for (Property<?> property : from.getProperties()) {
			result = copy(result, from, property);
		}
		return result;
	}

	private static <T extends Comparable<T>> BlockState copy(BlockState to, BlockState from, Property<T> property) {
		return to.contains(property) ? to.with(property, from.get(property)) : to;
	}
}
