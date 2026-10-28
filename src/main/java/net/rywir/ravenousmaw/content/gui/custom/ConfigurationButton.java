package net.rywir.ravenousmaw.content.gui.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.rywir.ravenousmaw.content.packet.payload.ConfigurationPayload;
import net.rywir.ravenousmaw.util.dstruct.ConfigAction;

public class ConfigurationButton extends Button {
    private final Identifier texture;
    private final String key;

    public ConfigurationButton(String key, int x, int y, int width, int height, Component message, OnPress onPress, Identifier texture) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
        this.key = key;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), 0, 0, width, height, width, height);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.isHovered()) return false;

        boolean isShiftDown = event.hasShiftDown();

        ConfigAction action;

        if (isShiftDown) {
            action = ConfigAction.RESET;
        } else if (event.button() == 1) {
            action = ConfigAction.DECREMENT;
        } else {
            action = ConfigAction.INCREMENT;
        }

        ClientPacketDistributor.sendToServer(
            new ConfigurationPayload(key, action)
        );

        this.playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }
}
