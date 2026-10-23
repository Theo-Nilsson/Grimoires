package io.github.theonilsson.grimoires.spell;

import io.github.theonilsson.grimoires.init.GrimoiresSpells;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public class SpellInstance {

	private final Identifier archetypeId;
	@Nullable private final Identifier elementId;
	private final Text name;
	@Nullable private final Integer color; // RGB mana color
	private final NbtCompound params;

	public SpellInstance(Identifier archetypeId, Text name, @Nullable Integer color, NbtCompound params) {
		this(archetypeId, null, name, color, params);
	}

	public SpellInstance(Identifier archetypeId, @Nullable Identifier elementId, Text name, @Nullable Integer color, NbtCompound params) {
		this.archetypeId = archetypeId;
		this.elementId = elementId;
		this.name = name;
		this.color = color;
		this.params = params;
	}

	public Identifier getArchetypeId() {
		return archetypeId;
	}

	public Text getName() {
		return name;
	}

	public @Nullable Integer getColor() {
		return color;
	}

	@Nullable
	public SpellArchetype archetype() {
		return GrimoiresSpells.ARCHETYPES.get(archetypeId);
	}

	@Nullable
	public SpellElement element() {
		return GrimoiresSpells.ELEMENTS.get(elementId);
	}

	public float get(SpellParam p) {
		if (params.contains(p.id(), NbtElement.NUMBER_TYPE)) {
			return p.clamp(params.getFloat(p.id()));
		}
		return p.defaultValue();
	}

	public SpellInstance with(SpellParam p, float value) {
		NbtCompound copy = params.copy();
		copy.putFloat(p.id(), p.clamp(value));
		return new SpellInstance(archetypeId, elementId, name, color, copy);
	}

	public SpellInstance copy() {
		return new SpellInstance(archetypeId, elementId, name, color, params);
	}

	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		nbt.putString("archetype", archetypeId.toString());
		if (elementId != null) nbt.putString("element", elementId.toString());
		nbt.putString("name", Text.Serializer.toJson(name));
		nbt.putInt("color", color != null ? color : -1);
		nbt.put("params", params.copy());
		return nbt;
	}

	public static SpellInstance fromNbt(NbtCompound nbt) {
		int color = nbt.getInt("color");
		return new SpellInstance(
			new Identifier(nbt.getString("archetype")),
			nbt.contains("element") ? new Identifier(nbt.getString("element")) : null,
			Text.Serializer.fromJson(nbt.getString("name")),
			color == -1 ? null : color,
			nbt.getCompound("params"));
	}
}
