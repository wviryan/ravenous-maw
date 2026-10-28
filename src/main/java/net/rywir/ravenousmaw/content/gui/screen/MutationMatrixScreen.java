package net.rywir.ravenousmaw.content.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.gui.menu.MutationMatrixMenu;
import org.jetbrains.annotations.NotNull;

public class MutationMatrixScreen extends AbstractContainerScreen<MutationMatrixMenu> {
    private static final Identifier GUI_TEXTURE = Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID,"textures/gui/mutation_matrix.png");
    private static final Identifier ARROW_TEXTURE = Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID,"textures/gui/progress_arrow.png");

    public MutationMatrixScreen(MutationMatrixMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics graphics, int x, int y) {
        if(menu.isCrafting()) {
            graphics.blit(ARROW_TEXTURE, x + 89, y + 35, 0, 0, menu.getScaledArrowProgress(), 16, 24, 16);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(graphics, pMouseX, pMouseY);
    }
}
