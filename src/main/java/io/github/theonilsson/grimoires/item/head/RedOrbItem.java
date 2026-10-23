package io.github.theonilsson.grimoires.item.head;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import io.github.theonilsson.grimoires.spell.SpellElement;
import io.github.theonilsson.grimoires.spell.SpellSchool;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.Map;

public class RedOrbItem extends Item implements StaffHead {

	private static final Map<SpellSchool, Float> AFFINITY = Map.of(
		GrimoiresSpells.OFFENSIVE, 1f,
		GrimoiresSpells.DEFENSIVE, 1.2f
	);

	public RedOrbItem(Settings settings) {
		super(settings);
	}

	@Override
	public Identifier getId() {
		return Grimoires.id("red_orb");
	}

	@Override
	public float getCastSpeedModifier() {
		return 0.7f;
	}

	@Override
	public float getManaCostModifier() {
		return 0.7f;
	}

	@Override
	public float getAffinity(SpellSchool school) {
		return AFFINITY.getOrDefault(school, 1.0f);
	}

	@Override
	public float getElementalAffinity(SpellElement element) {
		return 0.6f;
	}

	@Override
	public Grade getMagicGrade() {
		return Grade.FIRST_CLASS;
	}
}
