package io.github.theonilsson.grimoires.item.relics;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import io.github.theonilsson.grimoires.init.GrimoiresItems;
import io.github.theonilsson.grimoires.item.HasMagicGrade;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Optional;

public class MajaciteCrownItem extends TrinketItem implements HasMagicGrade {
	public MajaciteCrownItem(Settings settings) {
		super(settings);
	}

	@Override
	public Grade getMagicGrade() {
		return Grade.AGE_OLD;
	}

	public static boolean hasMajaciteCrownEquipped(LivingEntity entity) {
		Optional<TrinketComponent> compOpt = TrinketsApi.getTrinketComponent(entity);
		if (compOpt.isEmpty()) return false;
		TrinketComponent comp = compOpt.get();
		List<Pair<SlotReference, ItemStack>> equipped = comp.getEquipped(GrimoiresItems.MAJACITE_CROWN_ITEM);
		for (Pair<SlotReference, ItemStack> ignored : equipped) {
			return true;
		}
		return false;
	}
}
