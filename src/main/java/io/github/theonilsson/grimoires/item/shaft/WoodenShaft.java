package io.github.theonilsson.grimoires.item.shaft;

import io.github.theonilsson.grimoires.Grimoires;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class WoodenShaft extends Item implements StaffShaft {
	public WoodenShaft(Settings settings) {
		super(settings);
	}

	@Override
	public Identifier getId() {
		return Grimoires.id("wood");
	}

	@Override
	public float getManaRegenModifier() {
		return 1.25f;
	}
}
