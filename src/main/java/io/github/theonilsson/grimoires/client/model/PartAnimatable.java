package io.github.theonilsson.grimoires.client.model;

import io.github.theonilsson.grimoires.client.renderer.StaffModel;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class PartAnimatable implements IAnimatable {

	private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
	public StaffModel model;

	@Override
	public void registerControllers(AnimationData animationData) {
	}

	@Override
	public AnimationFactory getFactory() {
		return animationFactory;
	}
}
