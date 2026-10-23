package io.github.theonilsson.grimoires.item.head;

import io.github.theonilsson.grimoires.item.StaffPart;
import io.github.theonilsson.grimoires.spell.SpellArchetype;
import io.github.theonilsson.grimoires.spell.SpellElement;
import io.github.theonilsson.grimoires.spell.SpellParam;
import io.github.theonilsson.grimoires.spell.SpellSchool;
import io.github.theonilsson.grimoires.spell.StaffStats;

import javax.annotation.Nullable;

public interface StaffHead extends StaffPart {
	@Override
	default String getSlot() {
		return "head";
	}

	default float getAffinity(SpellArchetype archetype) { return 1.0f; }
	default float getAffinity(SpellSchool school) { return 1.0f; }
	default float getElementalAffinity(SpellElement element) { return 1.0f; }

	default float modifyParam(SpellArchetype archetype, SpellParam param, float value) { return value; }
}
