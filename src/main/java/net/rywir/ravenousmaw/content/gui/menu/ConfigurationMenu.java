package net.rywir.ravenousmaw.content.gui.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.MenuTypes;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.AssimilationHandler;
import net.rywir.ravenousmaw.system.MutationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationMenu extends AbstractContainerMenu {
    private final ItemStack stack;
    private final Player player;
    private final Level level;
    private final MutationHandler handler;

    public ConfigurationMenu(int id, Inventory inv, ItemStack stack) {
        super(MenuTypes.CONFIGURATION_MENU.get(), id);
        this.stack = stack;
        this.player = inv.player;
        this.level = inv.player.level();
        this.handler = new MutationHandler(stack);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int i) {
        // No slots, nothing to move.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        // The menu edits this exact stack (main hand), so it closes once the Maw leaves that hand.
        return player.getMainHandItem() == stack;
    }

    public int configVal(Mutations.Parameters parameter) {
        if (parameter == Mutations.Parameters.ELDRITCH_ANTENNA) {
            return AssimilationHandler.getAntennaSwitch(player);
        }

        return this.handler.getConfigVal(parameter);
    }

    /** The Maw's mutation parameters, plus the Antenna switch once the player has earned that assimilation. */
    public List<Mutations.Parameters> parameters() {
        List<Mutations.Parameters> parameters = new ArrayList<>(this.handler.getAllParameters());

        if (AssimilationHandler.has(player, Assimilations.ELDRITCH_ANTENNA)) {
            parameters.add(Mutations.Parameters.ELDRITCH_ANTENNA);
        }

        return parameters;
    }

    public ItemStack getStack() {
        return stack;
    }

    public Level getLevel() {
        return level;
    }
}
