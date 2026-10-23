package io.github.theonilsson.grimoires.item.shaft;

import io.github.theonilsson.grimoires.item.StaffPart;

public interface StaffShaft extends StaffPart {
	@Override
	default String getSlot() {
		return "shaft";
	}
	default float getManaRegenModifier() { return 1.0f; }
}
