package io.github.theonilsson.grimoires.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.emi.trinkets.api.TrinketsApi;
import io.github.theonilsson.grimoires.component.MagicComponent;
import io.github.theonilsson.grimoires.init.GrimoiresItems;
import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import io.github.theonilsson.grimoires.item.PersonalGrimoireItem;
import io.github.theonilsson.grimoires.item.ScrollItem;
import io.github.theonilsson.grimoires.item.StaffItem;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.List;

public class GrimoiresCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("grimoires").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
			.then(CommandManager.literal("give")
				.then(CommandManager.literal("staff")
					.then(CommandManager.argument("shaft", IdentifierArgumentType.identifier())
						.executes(GrimoiresCommand::createStaff)
						.then(CommandManager.argument("ornament", IdentifierArgumentType.identifier())
							.executes(GrimoiresCommand::createStaff)
							.then(CommandManager.argument("head", IdentifierArgumentType.identifier())
								.executes(GrimoiresCommand::createStaff)))))
				.then(CommandManager.literal("scroll")
					.then(CommandManager.literal("zoltraak").executes((context) -> createScroll(context, GrimoiresSpells.ZOLTRAAK)))
					.then(CommandManager.literal("fire_beam").executes((context) -> createScroll(context, GrimoiresSpells.FIRE_RAY)))
					.then(CommandManager.literal("ice_beam").executes((context) -> createScroll(context, GrimoiresSpells.ICE_RAY)))
					.then(CommandManager.literal("barrier").executes(context ->  createScroll(context, GrimoiresSpells.BARRIER)))))
			.then(CommandManager.literal("mana")
				.then(CommandManager.literal("get").executes(GrimoiresCommand::getMana).then(CommandManager.literal("max").executes(GrimoiresCommand::getMaxMana)))
				.then(CommandManager.literal("set").then(CommandManager.argument("mana", IntegerArgumentType.integer())
					.executes(GrimoiresCommand::setMana))))
			.then(CommandManager.literal("selected")
				.then(CommandManager.literal("set").then(CommandManager.argument("idx", IntegerArgumentType.integer()).executes(GrimoiresCommand::setSelectedSpell))))
			.then(CommandManager.literal("spells").executes(context -> {
				PlayerEntity player = context.getSource().getPlayer();
				TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
					trinketComponent.getEquipped(stack -> stack.isOf(GrimoiresItems.PERSONAL_GRIMOIRE_ITEM)).forEach(pair -> {
						ItemStack itemStack = pair.getRight();
						List<SpellInstance> list = PersonalGrimoireItem.getSpells(itemStack);
						MutableText spells = Text.literal("");
						list.forEach(spellInstance -> {
							spells.append(Text.literal("\n").append(spellInstance.getName()));
						});
						player.sendMessage(spells, false);
					});
				});
				return 0;
			})));
	}

	private static Identifier getOptional(CommandContext<ServerCommandSource> ctx, String name) {
		try {
			return IdentifierArgumentType.getIdentifier(ctx, name);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static int createStaff(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		Identifier shaft = IdentifierArgumentType.getIdentifier(context, "shaft");
		Identifier ornament = getOptional(context, "ornament");
		Identifier head = getOptional(context, "head");

		ItemStack stack = new ItemStack(GrimoiresItems.STAFF_ITEM);
		StaffItem.setPart(stack, "shaft", shaft);
		StaffItem.setPart(stack, "ornament", ornament);
		StaffItem.setPart(stack, "head", head);
		context.getSource().getPlayer().getInventory().offerOrDrop(stack);

		return 0;
	}

	private static int createScroll(CommandContext<ServerCommandSource> context, SpellInstance spell) throws CommandSyntaxException {
		ItemStack stack = new ItemStack(GrimoiresItems.SCROLL_ITEM);
		ScrollItem.addSpell(stack, spell);
		context.getSource().getPlayer().getInventory().offerOrDrop(stack);
		return 0;
	}

	private static int getMana(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		PlayerEntity player = context.getSource().getPlayer();
		MagicComponent magic = MagicComponent.MAGIC.get(player);
		player.sendMessage(Text.literal("You have: " + magic.getMana() + " mana"), true);

		return 0;
	}

	private static int getMaxMana(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		PlayerEntity player = context.getSource().getPlayer();
		MagicComponent magic = MagicComponent.MAGIC.get(player);
		player.sendMessage(Text.literal("You have: " + magic.getMaxMana() + " max mana"), true);

		return 0;
	}

	private static int setMana(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		PlayerEntity player = context.getSource().getPlayer();
		MagicComponent magic = MagicComponent.MAGIC.get(player);
		int mana = IntegerArgumentType.getInteger(context, "mana");
		magic.setMana(mana);
		player.sendMessage(Text.literal("Mana set to " + magic.getMana()), true);

		MagicComponent.MAGIC.sync(player);

		return 0;
	}

	private static int setSelectedSpell(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		PlayerEntity player = context.getSource().getPlayer();
		ItemStack stack = player.getMainHandStack();
		int idx = IntegerArgumentType.getInteger(context, "idx");
		StaffItem.setSelectedSpellIndex(stack, idx);
		player.setStackInHand(Hand.MAIN_HAND, stack);
		return 0;
	}
}
