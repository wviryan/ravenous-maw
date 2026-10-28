package net.rywir.ravenousmaw.content.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.gui.custom.ConfigurationButton;
import net.rywir.ravenousmaw.content.gui.custom.StatIcon;
import net.rywir.ravenousmaw.content.gui.menu.ConfigurationMenu;
import net.rywir.ravenousmaw.registry.Mutations;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationScreen extends AbstractContainerScreen<ConfigurationMenu> {
    private static final int COLUMNS = 2;
    private static final int ROWS = 6;

    private static final int TEXTURE_SIZE = 16;

    /** Page 0 holds the settings, page 1 the read-only values (see ConfigurationMenu#stats). */
    private static final int PAGE_SETTINGS = 0;
    private static final int PAGE_VALUES = 1;

    private static final Font font = Minecraft.getInstance().font;

    private final Map<String, StringWidget> widgets;

    /** Kept across rebuilds (e.g. a window resize) so the screen stays on the page the player chose. */
    private int page = PAGE_SETTINGS;

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        RavenousMaw.MOD_ID,
        "textures/gui/configuration_menu.png"
    );

    public ConfigurationScreen(ConfigurationMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.widgets = new HashMap<>();
    }

    @Override
    protected void init() {
        super.init();
        widgets.clear();

        boolean hasValuesPage = !this.menu.stats().isEmpty();

        if (!hasValuesPage) {
            page = PAGE_SETTINGS;
        }

        if (page == PAGE_SETTINGS) {
            addParameterCells();
        } else {
            addStatCells();
        }

        if (hasValuesPage) {
            addPageControls();
        }
    }

    /** First page: one cell per configurable parameter (clickable). */
    private void addParameterCells() {
        int cellWidth = this.imageWidth / COLUMNS;
        int cellHeight = this.imageHeight / ROWS;

        int index = 0;
        for (var parameter : this.menu.parameters()) {
            int rowPos = this.leftPos + ((index % COLUMNS) * cellWidth);
            int colPos = this.topPos + ((index / COLUMNS) * cellHeight);

            int confval = this.menu.configVal(parameter);

            int MARGIN = (cellHeight - TEXTURE_SIZE) / 2;

            Button button = new ConfigurationButton(
                parameter.key(),
                rowPos + MARGIN,
                colPos + MARGIN,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                Component.literal(String.valueOf(confval)),
                b -> {},
                parameter.icon()
            );

            button.setTooltip(Tooltip.create(
                Component.empty()
                    .append(parameter.title().copy().withStyle(ChatFormatting.GOLD))
                    .append("\n")
                    .append(parameter.description())
            ));

            this.addRenderableWidget(
                button
            );

            StringWidget widget = new StringWidget(
                rowPos + TEXTURE_SIZE + MARGIN * 3,
                colPos + MARGIN,
                TEXTURE_SIZE * 3,
                TEXTURE_SIZE,
                parameter.type() == Mutations.Parameters.Type.TOGGLE ? (confval == 1 ? Component.translatable("screen.ravenousmaw.active") : Component.translatable("screen.ravenousmaw.inactive")) : Component.literal(String.valueOf(confval)),
                font
            );

            this.addRenderableWidget(widget);

            widgets.put(parameter.key(), widget);

            index++;
        }
    }

    /** Second page: the read-only values (kills, stacks), laid out like the first. */
    private void addStatCells() {
        int cellWidth = this.imageWidth / COLUMNS;
        int cellHeight = this.imageHeight / ROWS;

        int index = 0;
        for (var stat : this.menu.stats()) {
            if (index >= COLUMNS * ROWS) break;

            int rowPos = this.leftPos + ((index % COLUMNS) * cellWidth);
            int colPos = this.topPos + ((index / COLUMNS) * cellHeight);

            int MARGIN = (cellHeight - TEXTURE_SIZE) / 2;

            StatIcon icon = new StatIcon(rowPos + MARGIN, colPos + MARGIN, TEXTURE_SIZE, TEXTURE_SIZE, stat.title(), stat.icon());

            icon.setTooltip(Tooltip.create(
                Component.empty()
                    .append(stat.title().copy().withStyle(ChatFormatting.GOLD))
                    .append("\n")
                    .append(stat.description())
            ));

            this.addRenderableWidget(icon);

            StringWidget widget = new StringWidget(
                rowPos + TEXTURE_SIZE + MARGIN * 3,
                colPos + MARGIN,
                TEXTURE_SIZE * 3,
                TEXTURE_SIZE,
                Component.literal(stat.valueText()),
                font
            );

            this.addRenderableWidget(widget);

            widgets.put(stat.key(), widget);

            index++;
        }
    }

    /** Previous/next buttons and the page name, under the panel. Only shown when there is a second page. */
    private void addPageControls() {
        int y = this.topPos + this.imageHeight + 4;
        int centerX = this.leftPos + this.imageWidth / 2;

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> switchPage(PAGE_SETTINGS))
            .bounds(centerX - 50, y, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> switchPage(PAGE_VALUES))
            .bounds(centerX + 30, y, 20, 20).build());

        Component name = Component.translatable(page == PAGE_SETTINGS ? "screen.ravenousmaw.page_settings" : "screen.ravenousmaw.page_values");
        StringWidget label = new StringWidget(centerX - 28, y + 6, 56, 10, name, font);
        label.alignCenter();

        this.addRenderableWidget(label);
    }

    private void switchPage(int newPage) {
        this.page = newPage;
        this.rebuildWidgets();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float v, int i, int i1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {}

    public void update(String key, int value) {
        StringWidget widget = widgets.get(key);

        if (widget == null) return;

        for (var stat : this.menu.stats()) {
            if (stat.key().equals(key)) {
                widget.setMessage(Component.literal(stat.withValue(value).valueText()));
                return;
            }
        }

        Mutations.Parameters parameter = Mutations.Parameters.byKey(key);

        widget.setMessage(parameter.type() == Mutations.Parameters.Type.TOGGLE ? (value == 1 ? Component.translatable("screen.ravenousmaw.active") : Component.translatable("screen.ravenousmaw.inactive")) : Component.literal(String.valueOf(value)));
    }
}
