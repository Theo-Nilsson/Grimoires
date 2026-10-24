package io.github.theonilsson.grimoires.spell;

import io.github.theonilsson.grimoires.item.relics.MajaciteCrownItem;
import net.minecraft.entity.player.PlayerEntity;

public class MagicSuppression {

	// A value between 0 and 1. 0 means disabled, 1 means fully active.
	public static float of(PlayerEntity player) {
		return MajaciteCrownItem.hasMajaciteCrownEquipped(player) ? 0 : 1;
	}
}
