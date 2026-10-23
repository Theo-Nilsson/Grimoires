package io.github.theonilsson.grimoires.client.screen;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import io.github.theonilsson.grimoires.init.GrimoiresItems;
import io.github.theonilsson.grimoires.init.GrimoiresPackets;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.Optional;

public class ScrollScreen extends Screen {

	private final SpellInstance spell;
	private ButtonWidget learnButton;

	public ScrollScreen(SpellInstance spell) {
		super(Text.translatable("screen.grimoires.scroll"));
		this.spell = spell;
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		learnButton = new ButtonWidget(
			centerX - 105,
			centerY,
			100,
			20,
			Text.literal("Learn"),
			button -> {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeNbt(spell.toNbt());
				ClientPlayNetworking.send(GrimoiresPackets.LEARN_SPELL_ID, buf);
				MinecraftClient.getInstance().setScreen(null);
			},
			((buttonWidget, matrices, i, j) -> {
				this.renderTooltip(matrices, Text.translatable("screen.grimoires.scroll.tooltip"), i, j);
			})
		);


		this.addDrawableChild(learnButton);
		this.addDrawableChild(
			new ButtonWidget(
				centerX + 5,
				centerY,
				100,
				20,
				Text.literal("Cancel"),
				button -> {
					MinecraftClient.getInstance().setScreen(null);
				}
			)
		);

		updateLearnButton();
	}

	@Override
	public void render(
		MatrixStack matrices,
		int mouseX,
		int mouseY,
		float delta
	) {
		this.renderBackground(matrices);

		drawCenteredText(
			matrices,
			this.textRenderer,
			Text.translatable("text.grimoires.learn_spell", spell.getName().getString()),
			this.width / 2,
			this.height / 2 - 30,
			0xFFFFFF
		);

		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void tick() {
		super.tick();
		updateLearnButton();
	}

	private void updateLearnButton() {
		if (learnButton == null) return;

		learnButton.active = checkCondition();
	}

	private boolean checkCondition() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		Optional<TrinketComponent> compOpt = TrinketsApi.getTrinketComponent(player);
		return compOpt.map(trinketComponent -> trinketComponent.isEquipped(GrimoiresItems.PERSONAL_GRIMOIRE_ITEM)).orElse(false);
	}
}
