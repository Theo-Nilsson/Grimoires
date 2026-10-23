package io.github.theonilsson.grimoires.init;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.github.theonilsson.grimoires.component.MagicComponent;
import org.jetbrains.annotations.NotNull;

public class GrimoiresComponents implements EntityComponentInitializer {

	@Override
	public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(MagicComponent.MAGIC, MagicComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
	}
}
