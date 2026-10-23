package io.github.theonilsson.grimoires;

import io.github.theonilsson.grimoires.command.GrimoiresCommand;
import io.github.theonilsson.grimoires.component.MagicComponent;
import io.github.theonilsson.grimoires.init.GrimoiresItems;
import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import io.github.theonilsson.grimoires.init.GrimoiresStaffParts;
import io.github.theonilsson.grimoires.item.StaffItem;
import io.github.theonilsson.grimoires.item.StaffPart;
import io.github.theonilsson.grimoires.item.ornament.StaffOrnament;
import io.github.theonilsson.grimoires.item.shaft.StaffShaft;
import io.github.theonilsson.grimoires.networking.LearnSpellC2SPacket;
import io.github.theonilsson.grimoires.spell.BarrierManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

import java.util.Optional;

public class Grimoires implements ModInitializer {

	public static final String MOD_ID = "grimoires";
	public static final Logger LOGGER = LoggerFactory.getLogger("Grimoires");

	public static final EntityAttribute MAX_MANA = Registry.register(Registry.ATTRIBUTE,
		Grimoires.id("max_mana"),
		new ClampedEntityAttribute("attribute.name.grimoires.max_mana", 80.0, 0.0, 10000.0)
			.setTracked(true));

	private static final ThreadLocal<Boolean> DAMAGING = ThreadLocal.withInitial(() -> false);

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());
		GeckoLib.initialize();

		GrimoiresItems.init();
		GrimoiresSpells.init();
		GrimoiresStaffParts.init();

		BarrierManager.init();

		CommandRegistrationCallback.EVENT.register(((dispatcher, buildContext, environment) -> {
			GrimoiresCommand.register(dispatcher);
		}));
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof PlayerEntity player) || DAMAGING.get()) {
				return true;
			}

			float modified = applyStaffOrnament(player, source, amount);

			if (modified != amount) {
				DAMAGING.set(true);
				try {
					player.damage(source, modified);
				} finally {
					DAMAGING.set(false);
				}
				return false;
			}

			return true;
		});
		ServerTickEvents.START.register((server) -> {
			server.getPlayerManager().getPlayerList().forEach(player -> {
				MagicComponent magic = MagicComponent.MAGIC.get(player);
				ItemStack stack = player.getMainHandStack();
				float magicRegenMult = 1.0f;
				if (stack.isOf(GrimoiresItems.STAFF_ITEM)) {
					Optional<StaffPart> partOpt = GrimoiresStaffParts.get(StaffItem.getPart(stack, "shaft"));
					if (partOpt.isPresent() && partOpt.get() instanceof StaffShaft shaft) {
						magicRegenMult = shaft.getManaRegenModifier();
					}
				}
				magic.setMana(magic.getMana() + 0.05f * magicRegenMult);
				MagicComponent.MAGIC.sync(player);
			});
		});

		LearnSpellC2SPacket.register();
	}

	private static float applyStaffOrnament(PlayerEntity player, DamageSource source, float amount) {
		ItemStack staff = player.getMainHandStack();
		if (!staff.isOf(GrimoiresItems.STAFF_ITEM)) return amount;

		Optional<StaffPart> part = GrimoiresStaffParts.get(StaffItem.getPart(staff, "ornament"));
		if (part.isPresent() && part.get() instanceof StaffOrnament ornament) {
			return ornament.onIncomingDamage(player, source, amount, staff);
		}

		return amount;
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
