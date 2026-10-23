package io.github.theonilsson.grimoires.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;

public class MagicComponent implements AutoSyncedComponent {

	public static final ComponentKey<MagicComponent> MAGIC =
			ComponentRegistry.getOrCreate(Grimoires.id("magic"), MagicComponent.class);

	private final PlayerEntity player;

	private float mana;
	private boolean shouldSpendMana;

	public MagicComponent(PlayerEntity player) {
		this.player = player;
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		if (tag.contains("Mana")) this.mana = tag.getFloat("Mana");
		else this.mana = 80f;
		if (tag.contains("ShouldSpendMana")) this.shouldSpendMana = tag.getBoolean("ShouldSpendMana");
		else this.shouldSpendMana = false;
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		tag.putFloat("Mana", mana);
		tag.putBoolean("ShouldSpendMana", shouldSpendMana);
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

	public boolean shouldSpendMana() {
		return shouldSpendMana;
	}

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
