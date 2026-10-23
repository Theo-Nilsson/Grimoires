package io.github.theonilsson.grimoires.init;

import io.github.theonilsson.grimoires.item.StaffPart;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GrimoiresStaffParts {

	private static final Map<Identifier, StaffPart> PARTS = new HashMap<>();

	public static void register(StaffPart part) {
		PARTS.put(part.getId(), part);
	}

	public static Optional<StaffPart> get(Identifier id) {
		return Optional.ofNullable(PARTS.get(id));
	}

	public static void init() {}

	static {
		register((StaffPart) GrimoiresItems.RED_ORB_ITEM);
		register((StaffPart) GrimoiresItems.METAL_RING_ITEM);
		register((StaffPart) GrimoiresItems.WOODEN_STAFF_ITEM);
	}
}
