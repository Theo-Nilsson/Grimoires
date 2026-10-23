package io.github.theonilsson.grimoires.client.renderer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.theonilsson.grimoires.client.StaffModels;
import io.github.theonilsson.grimoires.spell.StaffSlots;
import io.github.theonilsson.grimoires.client.model.PartAnimatable;
import io.github.theonilsson.grimoires.client.model.PartModel;
import io.github.theonilsson.grimoires.item.StaffItem;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.RenderUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StaffRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, IGeoRenderer<PartAnimatable> {

	private final PartModel partModel = new PartModel();
	private VertexConsumerProvider rtb;
	private final Cache<String, PartAnimatable> animatables = CacheBuilder.newBuilder()
		.expireAfterAccess(30, TimeUnit.SECONDS).build();

	private Map<String, StaffModel> getStaffModels(ItemStack stack) {
		Map<String, StaffModel> bySlot = new HashMap<>();
		for (String slot : StaffSlots.all()) {
			Identifier partId = StaffItem.getPart(stack, slot);
			if (partId != null) StaffModels.get(partId).ifPresent(m -> bySlot.put(slot, m));
		}
		return bySlot;
	}

	@Override
	public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices,
					   VertexConsumerProvider buffers, int light, int overlay) {
		Map<String, StaffModel> bySlot = getStaffModels(stack);
		if (bySlot.isEmpty()) return;

		// parent slot -> children (null key = roots)
		Map<String, List<StaffModel>> children = new HashMap<>();
		for (StaffModel m : bySlot.values()) {
			String parent = StaffSlots.parentOf(m.slot());
			if (parent != null && !bySlot.containsKey(parent)) continue; // orphan: parent missing
			children.computeIfAbsent(parent, k -> new ArrayList<>()).add(m);
		}

		UUID id = stack.hasNbt() && stack.getNbt().containsUuid(StaffItem.UUID_KEY)
			? stack.getNbt().getUuid(StaffItem.UUID_KEY) : new UUID(0, 0);

		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		for (StaffModel root : children.getOrDefault(null, List.of()))
			renderTree(root, children, id, stack, matrices, buffers, light, overlay);
		matrices.pop();
	}

	private void renderTree(StaffModel model, Map<String, List<StaffModel>> children, UUID id,
							ItemStack stack, MatrixStack matrices, VertexConsumerProvider buffers,
							int light, int overlay) {
		PartAnimatable anim;
		try {
			anim = animatables.get(id + "/" + model.id(), PartAnimatable::new);
		} catch (ExecutionException e) { return; }
		anim.model = model;

		GeoModel geo = partModel.getModel(partModel.getModelResource(anim));
		float tick = MinecraftClient.getInstance().getTickDelta();
		partModel.setCustomAnimations(anim, id.hashCode() ^ model.id().hashCode(),
			new AnimationEvent<>(anim, 0, 0, tick, false, Collections.singletonList(stack)));

		RenderLayer layer = model.translucent()
			? RenderLayer.getEntityTranslucent(model.texture())
			: RenderLayer.getEntityCutoutNoCull(model.texture());
		VertexConsumer vc = ItemRenderer.getDirectItemGlintConsumer(buffers, layer, true, stack.hasGlint());
		float r = (model.tint() >> 16 & 255) / 255f, g = (model.tint() >> 8 & 255) / 255f, b = (model.tint() & 255) / 255f;

		setCurrentRTB(buffers);
		matrices.push();
		render(geo, anim, tick, layer, matrices, buffers, vc, light, overlay, r, g, b, 1f);

		for (StaffModel child : children.getOrDefault(model.slot(), List.of())) {
			matrices.push();
			if (child.attachBone() != null) applyBoneTransform(matrices, geo, child.attachBone());
			renderTree(child, children, id, stack, matrices, buffers, light, overlay);
			matrices.pop();
		}
		matrices.pop();
	}

	private static void applyBoneTransform(MatrixStack m, GeoModel geo, String name) {
		GeoBone target = geo.getBone(name).orElse(null);
		if (target == null) return;
		Deque<GeoBone> chain = new ArrayDeque<>();
		for (GeoBone b = target; b != null; b = b.parent) chain.push(b);
		while (chain.size() > 1) RenderUtils.prepMatrixForBone(m, chain.pop());
		GeoBone last = chain.pop();
		RenderUtils.translateMatrixToBone(m, last);
		RenderUtils.translateToPivotPoint(m, last);
		RenderUtils.rotateMatrixAroundBone(m, last);
	}

	@Override
	public VertexConsumerProvider getCurrentRTB() {
		return rtb;
	}

	@Override
	public void setCurrentRTB(VertexConsumerProvider bufferSource) {
		this.rtb = bufferSource;
	}

	@Override
	public AnimatedGeoModel<PartAnimatable> getGeoModelProvider() {
		return partModel;
	}

	@Override
	public Identifier getTextureLocation(PartAnimatable animatable) {
		return this.getGeoModelProvider().getTextureResource(animatable);
	}

	@Override
	public Identifier getTextureResource(PartAnimatable instance) {
		return this.getGeoModelProvider().getTextureResource(instance);
	}
}


