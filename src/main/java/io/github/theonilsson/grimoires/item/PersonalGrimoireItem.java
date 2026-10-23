package io.github.theonilsson.grimoires.item;

import dev.emi.trinkets.api.TrinketItem;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class PersonalGrimoireItem extends TrinketItem {

	private static final String SPELLS_KEY = "Spells";

	public PersonalGrimoireItem(Settings settings) {
		super(settings);
	}

	public static List<SpellInstance> getSpells(ItemStack stack) {
		NbtCompound tag = stack.getOrCreateNbt();
		List<SpellInstance> spells = new ArrayList<>();
		NbtList list = tag.getList(SPELLS_KEY, NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < list.size(); i++) {
			NbtCompound spellTag = list.getCompound(i);
			spells.add(SpellInstance.fromNbt(spellTag));
		}
		return spells;
	}

	public static void addSpell(ItemStack stack, SpellInstance spell) {
		NbtCompound tag = stack.getOrCreateNbt();
		NbtList list = tag.getList(SPELLS_KEY, NbtElement.COMPOUND_TYPE);
		list.add(spell.toNbt());
		tag.put(SPELLS_KEY, list);
	}
}
