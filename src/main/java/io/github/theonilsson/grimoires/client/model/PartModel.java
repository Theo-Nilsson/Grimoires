package io.github.theonilsson.grimoires.client.model;

import io.github.theonilsson.grimoires.Grimoires;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PartModel extends AnimatedGeoModel<PartAnimatable> {

	private static final Identifier EMPTY_ANIM = Grimoires.id("animations/empty.animation.json");

	@Override
	public Identifier getModelResource(PartAnimatable a) {
		return a.model.model();
	}

	@Override
	public Identifier getTextureResource(PartAnimatable a) {
		return a.model.texture();
	}

	@Override
	public Identifier getAnimationResource(PartAnimatable a) {
		return EMPTY_ANIM;
	}
}
