package io.github.theonilsson.grimoires.init;

import io.github.theonilsson.grimoires.Grimoires;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.registry.Registry;

public class GrimoiresStatusEffects {

	public static final StatusEffect HASTENING;

	public static StatusEffect register(String name, StatusEffect effect) {
		return Registry.register(Registry.STATUS_EFFECT, Grimoires.id(name), effect);
	}

	public static void init() {}

	static {
		HASTENING = register("hastening", (new GrimoiresStatusEffect(StatusEffectType.BENEFICIAL, 0x3D70E0).addAttributeModifier(Grimoires.CASTING_SPEED, "4366a0db-d11e-40d0-b70f-65eb8634729d", 0.5, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)));
	}

	static class GrimoiresStatusEffect extends StatusEffect {
		protected GrimoiresStatusEffect(StatusEffectType type, int color) {
			super(type, color);
		}
	}
}
