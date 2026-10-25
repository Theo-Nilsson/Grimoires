package io.github.theonilsson.grimoires.init;

import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.spell.SpellArchetype;
import io.github.theonilsson.grimoires.spell.SpellElement;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import io.github.theonilsson.grimoires.spell.SpellSchool;
import io.github.theonilsson.grimoires.spell.archetype.BarrierArchetype;
import io.github.theonilsson.grimoires.spell.archetype.ErasureArchetype;
import io.github.theonilsson.grimoires.spell.archetype.RayArchetype;
import io.github.theonilsson.grimoires.spell.archetype.ReturnArchetype;
import io.github.theonilsson.grimoires.spell.element.AetherElement;
import io.github.theonilsson.grimoires.spell.element.FireElement;
import io.github.theonilsson.grimoires.spell.element.IceElement;
import io.github.theonilsson.grimoires.spell.element.NihilityElement;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class GrimoiresSpells {

	public static final SpellSchool OFFENSIVE, DEFENSIVE, PRIMAL;

	public static final SpellElement FIRE;
	public static final SpellElement ICE;
	public static final SpellElement AETHER;
	public static final SpellElement NIHILITY;

	public static final SpellArchetype RAY_ARCHETYPE;
	public static final SpellArchetype BARRIER_ARCHETYPE;
	public static final SpellArchetype ERASURE_ARCHETYPE;
	public static final SpellArchetype RETURN_ARCHETYPE;

	public static final SpellInstance ZOLTRAAK;
	public static final SpellInstance FIRE_RAY;
	public static final SpellInstance ICE_RAY;
	public static final SpellInstance BARRIER;
	public static final SpellInstance DOOR_TO_NOTHINGNESS;
	public static final SpellInstance RETURN_FROM_NOTHINGNESS;

	public static final RegistryKey<Registry<SpellArchetype>> ARCHETYPE_KEY =
		RegistryKey.ofRegistry(Grimoires.id("spell_archetype"));
	public static final RegistryKey<Registry<SpellSchool>> SCHOOL_KEY =
		RegistryKey.ofRegistry(Grimoires.id("spell_school"));
	public static final RegistryKey<Registry<SpellSchool>> ELEMENT_KEY =
		RegistryKey.ofRegistry(Grimoires.id("spell_element"));

	public static final Registry<SpellArchetype> ARCHETYPES =
		FabricRegistryBuilder.createSimple(SpellArchetype.class, ARCHETYPE_KEY.getValue())
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();
	public static final Registry<SpellSchool> SCHOOLS =
		FabricRegistryBuilder.createSimple(SpellSchool.class, SCHOOL_KEY.getValue())
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();
	public static final Registry<SpellElement> ELEMENTS =
		FabricRegistryBuilder.createSimple(SpellElement.class, ELEMENT_KEY.getValue())
			.attribute(RegistryAttribute.SYNCED)
			.buildAndRegister();

	private static SpellArchetype register(String name, SpellArchetype spell) {
		return Registry.register(ARCHETYPES, Grimoires.id(name), spell);
	}

	private static SpellElement register(String name, SpellElement school) {
		return Registry.register(ELEMENTS, Grimoires.id(name), school);
	}

	private static SpellSchool register(String name, SpellSchool school) {
		return Registry.register(SCHOOLS, Grimoires.id(name), school);
	}

	public static void init() {}

	static {
		OFFENSIVE = register("offensive", new SpellSchool(Grimoires.id("offensive"), 0xE8F4FF));
		DEFENSIVE = register("defensive", new SpellSchool(Grimoires.id("defensive"), 0xE8F4FF));
		PRIMAL = register("primal", new SpellSchool(Grimoires.id("primal"), 0xFFFFFF));

		FIRE = register("fire", new FireElement(2.0f));
		ICE = register("ice", new IceElement(2.0f));
		AETHER = register("aether", new AetherElement(10.0f));
		NIHILITY = register("nihility", new NihilityElement(10.0f));

		RAY_ARCHETYPE = register("ray", new RayArchetype());
		BARRIER_ARCHETYPE = register("barrier", new BarrierArchetype());
		ERASURE_ARCHETYPE = register("erasure", new ErasureArchetype());
		RETURN_ARCHETYPE = register("return", new ReturnArchetype());

		ZOLTRAAK = new SpellInstance(Grimoires.id("ray"), Text.literal("Zoltraak"), 0xE8F4FF, new NbtCompound());
		FIRE_RAY = new SpellInstance(Grimoires.id("ray"), Grimoires.id("fire"), Text.translatable("spell.grimoires.fire_zoltraak"), 0xFF0000, new NbtCompound());
		ICE_RAY = new SpellInstance(Grimoires.id("ray"), Grimoires.id("ice"), Text.translatable("spell.grimoires.ice_zoltraak"), 0x0000FF, new NbtCompound());
		BARRIER = new SpellInstance(Grimoires.id("barrier"), null, Text.translatable("spell.grimoires.barrier"), 0xE8F4FF, new NbtCompound());
		DOOR_TO_NOTHINGNESS = new SpellInstance(Grimoires.id("erasure"), Grimoires.id("aether"), Text.translatable("spell.grimoires.door_to_nothingness"), 0xFFFFF, new NbtCompound());
		RETURN_FROM_NOTHINGNESS = new SpellInstance(Grimoires.id("return"), Grimoires.id("nihility"), Text.translatable("spell.grimoires.return_from_nothingness"), 0x000000, new NbtCompound());
	}
}
