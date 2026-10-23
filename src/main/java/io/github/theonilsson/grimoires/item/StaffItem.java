package io.github.theonilsson.grimoires.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.init.GrimoiresItems;
import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import io.github.theonilsson.grimoires.init.GrimoiresStaffParts;
import io.github.theonilsson.grimoires.item.head.StaffHead;
import io.github.theonilsson.grimoires.item.ornament.StaffOrnament;
import io.github.theonilsson.grimoires.item.shaft.StaffShaft;
import io.github.theonilsson.grimoires.spell.SpellCaster;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import io.github.theonilsson.grimoires.spell.StaffStats;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StaffItem extends Item {

	public static final String UUID_KEY = "StaffId";
	public static final String PARTS_KEY = "Parts";
	public static final String SPELL_INDEX_KEY = "SpellIndex";

	private PlayerEntity currentPlayer;

	public StaffItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		user.setCurrentHand(hand);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 72000;
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if(world.isClient() || !(user instanceof ServerPlayerEntity player)) return;

		Optional<StaffPart> shaftOpt = GrimoiresStaffParts.get(getPart(stack, "shaft"));
		Optional<StaffPart> headOpt = GrimoiresStaffParts.get(getPart(stack, "head"));
		Optional<StaffPart> ornamentOpt = GrimoiresStaffParts.get(getPart(stack, "ornament"));

		SpellInstance spell = getSelectedSpell(player, stack);
		if (spell == null) return;
		StaffStats stats = StaffStats.of(spell, (StaffShaft) shaftOpt.orElse(null), (StaffHead) headOpt.orElse(null), (StaffOrnament) ornamentOpt.orElse(null));
		int charged = getMaxUseTime(stack) - remainingUseTicks;

		if (charged >= SpellCaster.castTicks(spell, stats)) {
			SpellCaster.tryCast(player, spell, stats);
		}
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		if (!(user instanceof ServerPlayerEntity player)) return;

		Optional<StaffPart> shaftOpt = GrimoiresStaffParts.get(getPart(stack, "shaft"));
		Optional<StaffPart> headOpt = GrimoiresStaffParts.get(getPart(stack, "head"));
		Optional<StaffPart> ornamentOpt = GrimoiresStaffParts.get(getPart(stack, "ornament"));

		SpellInstance spell = getSelectedSpell(player, stack);
		if (spell == null) return;
		StaffStats stats = StaffStats.of(spell, (StaffShaft) shaftOpt.orElse(null), (StaffHead) headOpt.orElse(null), (StaffOrnament) ornamentOpt.orElse(null));
		int charged = getMaxUseTime(stack) - remainingUseTicks;
		int required = Math.max(1, SpellCaster.castTicks(spell, stats));
		int pct = Math.min(100, Math.round(charged * 100f / required));

		player.sendMessage(Text.literal("Casting: " + pct + "%"), true);
	}

	public static void ensureId(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (!nbt.containsUuid(UUID_KEY)) nbt.putUuid(UUID_KEY, UUID.randomUUID());
	}

	@Override
	public void onCraft(ItemStack stack, World world, PlayerEntity player) {
		ensureId(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!world.isClient()) ensureId(stack);
		if (entity instanceof ServerPlayerEntity player) {
			this.currentPlayer = player;
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		if (currentPlayer != null) {
			SpellInstance spell = getSelectedSpell(currentPlayer, stack);
			if (spell != null) {
				tooltip.add(Text.translatable("tooltip.grimoires.staff.spell", spell.getName()).formatted(Formatting.GRAY));
			}
		}
		super.appendTooltip(stack, world, tooltip, context);
	}

	@Nullable
	public static NbtCompound getPartsTag(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null || !nbt.contains(PARTS_KEY, NbtElement.COMPOUND_TYPE)) return null;
		return nbt.getCompound(PARTS_KEY);
	}

	@Nullable
	public static Identifier getPart(ItemStack stack, String slot) {
		NbtCompound parts = getPartsTag(stack);
		if (parts == null || !parts.contains(slot, NbtElement.STRING_TYPE)) return null;
		return Identifier.tryParse(parts.getString(slot));
	}

	public static void setPart(ItemStack stack, String slot, @Nullable Identifier partType) {
		NbtCompound nbt = stack.getOrCreateNbt();
		NbtCompound parts = nbt.getCompound(PARTS_KEY); // empty compound if absent
		if (partType == null) parts.remove(slot);
		else parts.putString(slot, partType.toString());
		nbt.put(PARTS_KEY, parts);
	}

	public static int getSelectedSpellIndex(ItemStack stack) {
		NbtCompound nbt = stack.getOrCreateNbt();
		return nbt.contains(SPELL_INDEX_KEY) ? nbt.getInt(SPELL_INDEX_KEY) : -1;
	}

	public static void setSelectedSpellIndex(ItemStack stack, int spellIndex) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putInt(SPELL_INDEX_KEY, spellIndex);
	}

	public static @Nullable SpellInstance getSelectedSpell(PlayerEntity player, ItemStack stack) {
		int idx = getSelectedSpellIndex(stack);
		if (idx == -1) return null;
		Optional<TrinketComponent> trinketOpt = TrinketsApi.getTrinketComponent(player);
		if (trinketOpt.isEmpty()) return null;
		TrinketComponent trinket = trinketOpt.get();
		List<Pair<SlotReference, ItemStack>> equipped = trinket.getEquipped(GrimoiresItems.PERSONAL_GRIMOIRE_ITEM);
		if (equipped.isEmpty()) return null;
		for (Pair<SlotReference, ItemStack> pair : equipped) {
			ItemStack grimoireStack = pair.getRight();
			if (grimoireStack.isEmpty() || !grimoireStack.isOf(GrimoiresItems.PERSONAL_GRIMOIRE_ITEM)) continue;
			List<SpellInstance> spells = PersonalGrimoireItem.getSpells(grimoireStack);
			if (idx >= 0 && idx < spells.size()) {
			    return spells.get(idx);
			}
		}
		return null;
	}
}
