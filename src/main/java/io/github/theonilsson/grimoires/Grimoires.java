package io.github.theonilsson.grimoires;

import io.github.theonilsson.grimoires.command.GrimoiresCommand;
import io.github.theonilsson.grimoires.component.MagicComponent;
import io.github.theonilsson.grimoires.dungeon.DungeonManager;
import io.github.theonilsson.grimoires.dungeon.DungeonRoomRegistry;
import io.github.theonilsson.grimoires.dungeon.room.catacomb.CatacombCombatRoom;
import io.github.theonilsson.grimoires.dungeon.room.catacomb.CatacombStartingRoom;
import io.github.theonilsson.grimoires.init.*;
import io.github.theonilsson.grimoires.item.StaffItem;
import io.github.theonilsson.grimoires.item.StaffPart;
import io.github.theonilsson.grimoires.item.relics.MajaciteCrownItem;
import io.github.theonilsson.grimoires.item.shaft.StaffShaft;
import io.github.theonilsson.grimoires.networking.CycleSpellC2SPacket;
import io.github.theonilsson.grimoires.networking.LearnSpellC2SPacket;
import io.github.theonilsson.grimoires.spell.BarrierManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
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
	public static final EntityAttribute CASTING_SPEED = Registry.register(Registry.ATTRIBUTE,
		Grimoires.id("casting_speed"),
		new ClampedEntityAttribute("attribute.name.grimoires.casting_speed", 1.0, 0.0, 100.0)
			.setTracked(true));

	public static final RegistryKey<World> VOID_DIMENSION = RegistryKey.of(Registry.WORLD_KEY, id("void"));

	private static final ThreadLocal<Boolean> DAMAGING = ThreadLocal.withInitial(() -> false);

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());
		GeckoLib.initialize();

		GrimoiresItems.init();
		GrimoiresBlocks.init();
		GrimoiresSpells.init();
		GrimoiresStatusEffects.init();
		GrimoiresStaffParts.init();

		BarrierManager.init();

		Identifier catacomb = Grimoires.id("catacomb");
		DungeonRoomRegistry.register(catacomb, Grimoires.id("dungeon/0/starting_room"), CatacombStartingRoom::new);
		DungeonRoomRegistry.register(catacomb, Grimoires.id("dungeon/0/combat_room_1"), CatacombCombatRoom::new);

		CommandRegistrationCallback.EVENT.register(((dispatcher, buildContext, environment) -> {
			GrimoiresCommand.register(dispatcher);
		}));
		ServerLifecycleEvents.SERVER_STARTED.register(DungeonManager::loadAll);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> DungeonManager.clear());
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof PlayerEntity player) || DAMAGING.get()) {
				return true;
			}

			float modified = StaffItem.applyStaffOrnament(player, source, amount);

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
		ServerTickEvents.START.register((server) -> server.getPlayerManager().getPlayerList().forEach(player -> {
			MagicComponent magic = MagicComponent.MAGIC.get(player);
			if (MajaciteCrownItem.hasMajaciteCrownEquipped(player)) magic.growMaxManaFromMajacite();
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
		}));
		ServerWorldTickEvents.END.register((server, world) -> DungeonManager.tick(world));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (alive) return; // returning from the End, not a death
			if (oldPlayer.world.getRegistryKey() != VOID_DIMENSION) return;

			ServerWorld dim = newPlayer.server.getWorld(VOID_DIMENSION);
			if (dim == null || newPlayer.world == dim) return;

			BlockPos pos = dim.getSpawnPos();
			newPlayer.teleport(dim, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
				newPlayer.getYaw(), newPlayer.getPitch());
		});

		LearnSpellC2SPacket.register();
		CycleSpellC2SPacket.register();
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
