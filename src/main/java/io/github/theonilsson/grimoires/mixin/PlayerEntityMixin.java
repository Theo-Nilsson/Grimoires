package io.github.theonilsson.grimoires.mixin;

import io.github.theonilsson.grimoires.Grimoires;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Inject(method = "createPlayerAttributes", at = @At("RETURN"))
	private static void grimoires$createPlayerAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
		cir.getReturnValue().add(Grimoires.MAX_MANA);
	}
}
