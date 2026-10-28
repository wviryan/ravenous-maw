package net.rywir.ravenousmaw.content.gui.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** A configuration-screen icon with a tooltip and no click action, for read-only values such as a kill count. */
public class StatIcon extends AbstractWidget {
    private final ResourceLocation texture;

    public StatIcon(int x, int y, int width, int height, Component message, ResourceLocation texture) {
        super(x, y, width, height, message);
        this.texture = texture;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(texture, this.getX(), this.getY(), 0, 0, width, height, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
