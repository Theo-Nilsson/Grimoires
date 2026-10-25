package io.github.theonilsson.grimoires.networking;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.init.GrimoiresItems;
import io.github.theonilsson.grimoires.init.GrimoiresPackets;
import io.github.theonilsson.grimoires.item.StaffItem;
import net.minecraft.item.ItemStack;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class CycleSpellC2SPacket {

	public static void register() {
		Grimoires.LOGGER.info("Registering Cycle Spell receiver");
		ServerPlayNetworking.registerGlobalReceiver(GrimoiresPackets.CYCLE_SPELL_ID, (server, player, handler, buf, sender) -> {
			int change = buf.readInt();
			server.execute(() -> {
				ItemStack stack = player.getMainHandStack();
				if (!stack.isOf(GrimoiresItems.STAFF_ITEM)) return;
				StaffItem.setSelectedSpellIndex(stack, StaffItem.getSelectedSpellIndex(stack) + change);
				if (StaffItem.getSelectedSpellIndex(stack) < 0) StaffItem.setSelectedSpellIndex(stack, 0);
			});
		});
	}
}
