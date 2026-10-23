package io.github.theonilsson.grimoires.spell;

import net.minecraft.util.Identifier;

/**
 * @param color Default mana tint
 */
public record SpellSchool(Identifier id, int color) {

	public String translationKey() {
		return "spell_school." + this.id().getNamespace() + "." + this.id().getPath();
	}
}
