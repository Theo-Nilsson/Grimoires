package io.github.theonilsson.grimoires.spell;

import io.github.theonilsson.grimoires.item.HasMagicGrade;
import io.github.theonilsson.grimoires.item.head.StaffHead;
import io.github.theonilsson.grimoires.item.ornament.StaffOrnament;
import io.github.theonilsson.grimoires.item.shaft.StaffShaft;

public record StaffStats(float manaEfficiency, float castSpeed, float spellPower, HasMagicGrade.Grade rarity, ParamModifier modifier) {

    public StaffStats(float manaEfficiency, float castSpeed, float spellPower, HasMagicGrade.Grade rarity) {
        this(manaEfficiency, castSpeed, spellPower, rarity, (archetype, param, value) -> value);
    }

    public static StaffStats of(SpellInstance spell, StaffShaft s, StaffHead h, StaffOrnament o) {
        float cost = 1f;
        float speed = 1f;
		float power = 1f;
        if (s != null) {
            cost *= s.getManaCostModifier();
            speed *= s.getCastSpeedModifier();
			power *= s.getMagicPowerModifier();
        }
        if (h != null) {
            cost *= h.getManaCostModifier();
            speed *= h.getCastSpeedModifier();
			power *= h.getMagicPowerModifier();
        }
        if (o != null) {
            cost *= o.getManaCostModifier();
            speed *= o.getCastSpeedModifier();
			power *= o.getMagicPowerModifier();
        }
        float affinity = 1f;
        float archetypeAffinity = 1f;
        float elementAffinity = 1f;
        if (spell != null && h != null) {
            var archetype = spell.archetype();
            var element = spell.element();
            if (archetype != null) {
                affinity = h.getAffinity(archetype.getSpellSchool(spell));
                archetypeAffinity = h.getAffinity(archetype);
            }
            if (element != null) {
                elementAffinity = h.getElementalAffinity(element);
            }
        }
        cost *= affinity;
        cost *= archetypeAffinity;
        cost *= elementAffinity;
        if (affinity != 0f) speed *= 1f / affinity;
        if (archetypeAffinity != 0f) speed *= 1f / archetypeAffinity;
        if (elementAffinity !=  0f) speed *= 1f / elementAffinity;

		HasMagicGrade.Grade grade = HasMagicGrade.Grade.HEARTHWORN;
		if (s != null) {
			grade = s.getMagicGrade().getRank() < grade.getRank() ? s.getMagicGrade() : grade;
		}
		if (h != null) {
			grade = h.getMagicGrade().getRank() < grade.getRank() ? h.getMagicGrade() : grade;
		}
		if (o != null) {
			grade = o.getMagicGrade().getRank() < grade.getRank() ? o.getMagicGrade() : grade;
		}

        ParamModifier mod = (h != null)
                ? h::modifyParam
                : (archetype, param, value) -> value;

        return new StaffStats(cost, speed, power, grade, mod);
    }

    public float modifyParam(SpellArchetype archetype, SpellParam param, float value) {
        return modifier.modifyParam(archetype, param, value);
    }

    @FunctionalInterface
    public interface ParamModifier {
        float modifyParam(SpellArchetype archetype, SpellParam param, float value);
    }
}

