package io.github.theonilsson.grimoires.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.theonilsson.grimoires.Grimoires;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class MagicComponent implements AutoSyncedComponent {

	public static final ComponentKey<MagicComponent> MAGIC =
			ComponentRegistry.getOrCreate(Grimoires.id("magic"), MagicComponent.class);

	private static final String MAJACITE_MANA_BONUS_KEY = "MajaciteManaBonus";
	private static final UUID MAJACITE_MANA_BONUS_ID = UUID.fromString("d64e4952-271d-4aa9-baa3-264cc96c7c32");
	private static final float MAJACITE_MANA_GROWTH = 0.005f;
	private static final float MAJACITE_MANA_SCALE = 80f;

	private final PlayerEntity player;

	private float mana;
	private float majaciteManaBonus;
	private boolean shouldSpendMana;

	public MagicComponent(PlayerEntity player) {
		this.player = player;
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		if (tag.contains("Mana")) this.mana = tag.getFloat("Mana");
		else this.mana = 80f;
		if (tag.contains("ShouldSpendMana")) this.shouldSpendMana = tag.getBoolean("ShouldSpendMana");
		else this.shouldSpendMana = true;
		majaciteManaBonus = Math.max(0f, tag.getFloat(MAJACITE_MANA_BONUS_KEY));
		applyMajaciteManaBonus();
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		tag.putFloat("Mana", mana);
		tag.putBoolean("ShouldSpendMana", shouldSpendMana);
		tag.putFloat(MAJACITE_MANA_BONUS_KEY, majaciteManaBonus);
	}

	public float getMana() {
		return mana;
	}

	public void setMana(float mana) {
		this.mana = mana;
		this.mana = Math.min(this.mana, this.getMaxMana());
	}

	public int getMaxMana() {
		if (player == null) return 80;
		var instance = player.getAttributeInstance(Grimoires.MAX_MANA);
		if (instance == null) return 80;
		return (int) instance.getValue();
	}

	public void growMaxManaFromMajacite() {
		if (getMaxMana() >= 10000) return;

		majaciteManaBonus += MAJACITE_MANA_GROWTH / (1f + majaciteManaBonus / MAJACITE_MANA_SCALE);
		applyMajaciteManaBonus();
	}

	private void applyMajaciteManaBonus() {
		if (player == null) return;
		EntityAttributeInstance instance = player.getAttributeInstance(Grimoires.MAX_MANA);
		if (instance == null) return;

		instance.removeModifier(MAJACITE_MANA_BONUS_ID);
		if (majaciteManaBonus > 0f) {
			instance.addPersistentModifier(new EntityAttributeModifier(
					MAJACITE_MANA_BONUS_ID, "Majacite Crown mana", majaciteManaBonus,
					EntityAttributeModifier.Operation.ADDITION));
		}
	}

	public boolean shouldSpendMana() {
		return shouldSpendMana;
	} // Not working as expected? Check Grimoires::init (the tick event)

	public void setShouldSpendMana(boolean shouldSpendMana) {
		this.shouldSpendMana = shouldSpendMana;
	}

	public boolean tryConsume(float amount) {
		if (!shouldSpendMana()) return true;
		if (amount > getMana()) return false;
		setMana(getMana() - amount);
		return true;
	}
}
