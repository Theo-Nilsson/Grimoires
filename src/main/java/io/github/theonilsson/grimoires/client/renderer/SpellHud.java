package io.github.theonilsson.grimoires.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.theonilsson.grimoires.Grimoires;
import io.github.theonilsson.grimoires.component.MagicComponent;
import io.github.theonilsson.grimoires.init.GrimoiresItems;
import io.github.theonilsson.grimoires.item.StaffItem;
import io.github.theonilsson.grimoires.spell.SpellInstance;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class SpellHud {

	private static final Identifier TEXTURE = Grimoires.id("textures/gui/mana.png");
	private static final int TEX_W = 64, TEX_H = 32;
	private static final int BAR_W = 64, BAR_H = 16;

	public static void init() {
		HudRenderCallback.EVENT.register(SpellHud::render);
	}

	private static void render(MatrixStack matrices, float tickDelta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden || mc.player.isSpectator()) return;
		MagicComponent comp = MagicComponent.MAGIC.get(mc.player);

		float mana = comp.getMana();
		float maxMana = comp.getMaxMana();
		if (maxMana <= 0) return;

		float fraction = MathHelper.clamp(mana / maxMana, 0f, 1f);
		int percent = Math.round(fraction * 100);

		int x = 6;
		int y = mc.getWindow().getScaledHeight() - BAR_H - 6;

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.enableBlend();

		DrawableHelper.drawTexture(matrices, x, y, 0, 0, BAR_W, BAR_H, TEX_W, TEX_H);

		int filled = Math.round(BAR_W * fraction);
		if (filled > 0) {
			DrawableHelper.drawTexture(matrices, x, y,
				0, BAR_H,
				filled, BAR_H,
				TEX_W, TEX_H);
		}

		Text percentText = Text.literal(percent + "%");
		mc.textRenderer.drawWithShadow(matrices, percentText, x + BAR_W + 6, y + BAR_H - 10, 0x55AAFF);

		if (mc.player.getMainHandStack().isOf(GrimoiresItems.STAFF_ITEM)) {
			SpellInstance spell = StaffItem.getSelectedSpell(mc.player, mc.player.getMainHandStack());
			Text spellName = spell != null
				? spell.getName()
				: Text.literal("No spell selected (at index " + StaffItem.getSelectedSpellIndex(mc.player.getMainHandStack()) + ")");
			mc.textRenderer.drawWithShadow(matrices, spellName, x, y - 11, 0xFFFFFF);
		}

		RenderSystem.disableBlend();
	}
}
