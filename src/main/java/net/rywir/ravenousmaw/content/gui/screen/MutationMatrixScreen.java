package net.rywir.ravenousmaw.content.gui.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
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
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        extractProgressArrow(graphics, x, y);
    }

    private void extractProgressArrow(GuiGraphicsExtractor graphics, int x, int y) {
        if(menu.isCrafting()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ARROW_TEXTURE, x + 89, y + 35, 0.0F, 0.0F, menu.getScaledArrowProgress(), 16, 24, 16);
        }
    }
}
