package io.github.theonilsson.grimoires.client;

import io.github.theonilsson.grimoires.client.renderer.StaffModel;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StaffModels {

	public static final Map<Identifier, StaffModel> MODELS = new HashMap<>();

	public static void register(Identifier id, StaffModel model) {
		MODELS.put(id, model);
	}

	public static Optional<StaffModel> get(Identifier id) {
		return Optional.ofNullable(MODELS.get(id));
	}
}
