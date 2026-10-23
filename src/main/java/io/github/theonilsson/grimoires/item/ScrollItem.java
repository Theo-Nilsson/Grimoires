package io.github.theonilsson.grimoires.item;

import io.github.theonilsson.grimoires.init.GrimoiresPackets;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class ScrollItem extends Item {

	public static final String SPELL_KEY = "Spell";

	public ScrollItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (world.isClient()) return super.use(world, user, hand);
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeNbt(user.getStackInHand(hand).getOrCreateNbt().getCompound(SPELL_KEY));
		ServerPlayNetworking.send((ServerPlayerEntity) user, GrimoiresPackets.OPEN_SCREEN_ID, buf);
		return super.use(world, user, hand);
	}

	public static void addSpell(ItemStack stack, SpellInstance instance) {
		stack.getOrCreateNbt().put(SPELL_KEY, instance.toNbt());
	}
}
