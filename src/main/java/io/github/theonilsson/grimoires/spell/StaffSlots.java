package io.github.theonilsson.grimoires.spell;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class StaffSlots {
	public static final String SHAFT = "shaft", HEAD = "head", ORNAMENT = "ornament";

	private static final Map<String, String> PARENTS = new LinkedHashMap<>();
	static {
		PARENTS.put(SHAFT, null);     // root
		PARENTS.put(HEAD, SHAFT);
		PARENTS.put(ORNAMENT, SHAFT);
	}

	public static Set<String> all() { return PARENTS.keySet(); }
	public static @Nullable String parentOf(String slot) { return PARENTS.get(slot); }

	public static void register(String slot, @Nullable String parentSlot) { PARENTS.put(slot, parentSlot); }
}
