package io.github.theonilsson.grimoires.client;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.client.renderer.SpellHud;
import io.github.theonilsson.grimoires.client.renderer.StaffModel;
import io.github.theonilsson.grimoires.client.renderer.StaffRenderer;
import io.github.theonilsson.grimoires.init.GrimoiresItems;
import io.github.theonilsson.grimoires.networking.OpenScreenS2CPacket;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class GrimoiresClient implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		BuiltinItemRendererRegistry.INSTANCE.register(GrimoiresItems.STAFF_ITEM, new StaffRenderer());

		StaffModels.register(Grimoires.id("wood"), new StaffModel("wood_staff", "shaft", Grimoires.id("geo/wood_staff.geo.json"), new Identifier("textures/block/stripped_spruce_log.png"), null, StaffModel.NO_TINT));
		StaffModels.register(Grimoires.id("metal_ring"), new StaffModel("metal_ring", "ornament", Grimoires.id("geo/metal_ring.geo.json"), Grimoires.id("textures/ornament/metal_ring.png"), "ornament", StaffModel.NO_TINT));
		StaffModels.register(Grimoires.id("red_orb"), new StaffModel("red_orb", "head", Grimoires.id("geo/red_orb.geo.json"), Grimoires.id("textures/head/red_orb.png"), "head", StaffModel.NO_TINT, true));

		SpellHud.init();

		KeyBindingManager.init();

		OpenScreenS2CPacket.register();
	}
}
