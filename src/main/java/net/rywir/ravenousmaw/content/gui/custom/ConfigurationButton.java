package net.rywir.ravenousmaw.content.gui.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.PacketDistributor;
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
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        graphics.blit(texture, this.getX(), this.getY(), 0, 0, width, height, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isHovered()) return false;

        boolean isShiftDown = Screen.hasShiftDown();

        ConfigAction action;

        if (isShiftDown) {
            action = ConfigAction.RESET;
        } else if (button == 1) {
            action = ConfigAction.DECREMENT;
        } else {
            action = ConfigAction.INCREMENT;
        }

        PacketDistributor.sendToServer(
            new ConfigurationPayload(key, action)
        );

        this.playDownSound(Minecraft.getInstance().getSoundManager());
        return true;
    }
}
