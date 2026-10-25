package io.github.theonilsson.grimoires.client;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.theonilsson.grimoires.init.GrimoiresPackets;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class KeyBindingManager {

	public static KeyBind nextSpellKeybind;
	public static KeyBind prevSpellKeybind;

	public static void init() {
		ClientTickEvents.END.register(KeyBindingManager::tick);

		nextSpellKeybind = KeyBindingRegistryImpl.registerKeyBinding(new KeyBind(
			"keybind.grimoires.next_spell",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_X,
			KeyBind.GAMEPLAY_CATEGORY
		));
		prevSpellKeybind = KeyBindingRegistryImpl.registerKeyBinding(new KeyBind(
			"keybind.grimoires.prev_spell",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_C,
			KeyBind.GAMEPLAY_CATEGORY
		));
	}

	private static void tick(MinecraftClient minecraftClient) {
		while (nextSpellKeybind.wasPressed()) {
			if (minecraftClient.player == null) break;
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(1);
			ClientPlayNetworking.send(GrimoiresPackets.CYCLE_SPELL_ID, buf);
		}
		while (prevSpellKeybind.wasPressed()) {
			if (minecraftClient.player == null) break;
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(-1);
			ClientPlayNetworking.send(GrimoiresPackets.CYCLE_SPELL_ID, buf);
		}
	}
}
