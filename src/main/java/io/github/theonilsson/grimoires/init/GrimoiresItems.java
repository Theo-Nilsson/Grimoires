package io.github.theonilsson.grimoires.init;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.item.PersonalGrimoireItem;
import io.github.theonilsson.grimoires.item.ScrollItem;
import io.github.theonilsson.grimoires.item.StaffItem;
import io.github.theonilsson.grimoires.item.head.RedOrbItem;
import io.github.theonilsson.grimoires.item.ornament.MetalRingItem;
import io.github.theonilsson.grimoires.item.shaft.WoodenShaft;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class GrimoiresItems {

	public static final Item STAFF_ITEM;
	public static final Item PERSONAL_GRIMOIRE_ITEM;
	public static final Item SCROLL_ITEM;

	public static final Item RED_ORB_ITEM;
	public static final Item METAL_RING_ITEM;
	public static final Item WOODEN_STAFF_ITEM;

	public static Item register(String name, Item item) {
		return Registry.register(Registry.ITEM, Grimoires.id(name), item);
	}

	public static void init() {}

	static {
		STAFF_ITEM = register("staff", new StaffItem(new Item.Settings().maxCount(1)));
		PERSONAL_GRIMOIRE_ITEM = register("personal_grimoire", new PersonalGrimoireItem(new Item.Settings().maxCount(1)));
		SCROLL_ITEM = register("scroll", new ScrollItem(new Item.Settings().maxCount(1)));

		RED_ORB_ITEM = register("red_orb", new RedOrbItem(new Item.Settings()));
		METAL_RING_ITEM = register("metal_ring", new MetalRingItem(new Item.Settings()));
		WOODEN_STAFF_ITEM = register("wooden_staff", new WoodenShaft(new Item.Settings()));
	}
}
