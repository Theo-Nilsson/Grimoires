package io.github.theonilsson.grimoires.spell;

import java.util.List;

public abstract class SpellArchetype {

	public static final SpellParam RANGE  = new SpellParam("range", 8f, 48f, 24f, 0.6f);
	public static final SpellParam PIERCE = new SpellParam("pierce", 0f, 5f, 0f, 0.6f);
	public static final SpellParam DAMAGE = new SpellParam("damage", 2f, 20f, 6f, 3.0f);

	private final float baseManaCost;
	private final int baseCastTicks;

	public SpellArchetype(float baseManaCost, int baseCastTicks) {
		this.baseManaCost = baseManaCost;
		this.baseCastTicks = baseCastTicks;
	}

	public abstract SpellSchool getSpellSchool(SpellInstance spell);

	public abstract List<SpellParam> getParams();

	public abstract void cast(CastContext ctx);

	public int getColor(SpellInstance spell) {
		return spell.getColor() != null ? spell.getColor() : getSpellSchool(spell).color();
	}

	public float complexity(SpellInstance spell) {
		float total = 0f;
		for (SpellParam p : getParams()) {
			total += p.costWeight() * p.normalized(spell.get(p));
		}
		return total;
	}

	public float manaCost(SpellInstance spell) {
		return baseManaCost * (1f + complexity(spell));
	}

	public int castTicks(SpellInstance spell) {
		return Math.round(baseCastTicks * (1f + complexity(spell)));
	}
}
