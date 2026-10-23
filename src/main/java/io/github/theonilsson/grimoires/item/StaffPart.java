package io.github.theonilsson.grimoires.item;

import net.minecraft.util.Identifier;

public interface StaffPart extends HasMagicGrade {

	Identifier getId();

	String getSlot();

	default float getManaCostModifier()   { return 1.0f; } // Lower = cheaper
	default float getMagicPowerModifier() {	return 1.0f; }
	default float getCastSpeedModifier()  { return 1.0f; }

	@Override
	default Grade getMagicGrade() {
		return Grade.WAYFARER;
	}
}
