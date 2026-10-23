package io.github.theonilsson.grimoires.networking;

import dev.emi.trinkets.api.TrinketsApi;
import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.init.GrimoiresItems;
import io.github.theonilsson.grimoires.init.GrimoiresPackets;
import io.github.theonilsson.grimoires.item.PersonalGrimoireItem;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class LearnSpellC2SPacket {

	public static void register() {
		Grimoires.LOGGER.info("Registering Learn Spell receiver");
		ServerPlayNetworking.registerGlobalReceiver(GrimoiresPackets.LEARN_SPELL_ID, (server, player, handler, buf, sender) -> {
			NbtCompound tag = buf.readNbt();
			if (tag == null) return;
			server.execute(() -> {
				TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
					trinketComponent.getEquipped(stack -> stack.isOf(GrimoiresItems.PERSONAL_GRIMOIRE_ITEM)).forEach(pair -> {
						ItemStack itemStack = pair.getRight();
						PersonalGrimoireItem.addSpell(itemStack, SpellInstance.fromNbt(tag));
					});
				});
			});
		});
	}
}
