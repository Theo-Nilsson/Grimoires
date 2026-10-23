package io.github.theonilsson.grimoires.networking;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.client.screen.ScrollScreen;
import io.github.theonilsson.grimoires.init.GrimoiresPackets;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import net.minecraft.nbt.NbtCompound;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class OpenScreenS2CPacket {

	public static void register() {
		Grimoires.LOGGER.info("Registering Open Screen receiver");
		ClientPlayNetworking.registerGlobalReceiver(GrimoiresPackets.OPEN_SCREEN_ID, (client, clientPlayNetworkHandler, buf, sender) -> {
			NbtCompound nbt = buf.readNbt();
			if (nbt == null) return;
			client.execute(() -> {
				client.setScreen(new ScrollScreen(SpellInstance.fromNbt(nbt)));
			});
		});
	}
}
