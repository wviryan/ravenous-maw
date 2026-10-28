package net.rywir.ravenousmaw.content.gui.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.MenuTypes;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousAttachments;
import net.rywir.ravenousmaw.system.AssimilationHandler;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.util.Constants;
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

    /** Keys of the read-only values; the stacks key is also the sync key the server sends when the menu opens. */
    public static final String STAT_KILLS = "insatiable_voracity_kills";
    public static final String STAT_OXYTOCIN_STACKS = "oxytocin_stacks";

    /** A read-only value of the second page. {@code max} is 0 for a plain count, else it is shown as value/max. */
    public record Stat(String key, Component title, Component description, Identifier icon, int value, int max) {
        public Stat withValue(int newValue) {
            return new Stat(key, title, description, icon, newValue, max);
        }

        public String valueText() {
            return max > 0 ? value + "/" + max : String.valueOf(value);
        }
    }

    /**
     * Read-only values for the second page. The kill count is read from the item, which the client already has.
     * The Esca stacks live on the player; the client copy is filled by the sync sent when the menu opens.
     */
    public List<Stat> stats() {
        List<Stat> stats = new ArrayList<>();

        if (this.handler.has(Mutations.INSATIABLE_VORACITY)) {
            stats.add(new Stat(
                STAT_KILLS,
                Component.translatable("stat.ravenousmaw.kills"),
                Component.translatable("description.ravenousmaw.kills"),
                Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "textures/icon/insatiable_voracity_kills.png"),
                this.handler.getKills(),
                0
            ));
        }

        if (AssimilationHandler.has(player, Assimilations.OXYTOCINERGIC_ESCA)) {
            stats.add(new Stat(
                STAT_OXYTOCIN_STACKS,
                Component.translatable("stat.ravenousmaw.oxytocin_stacks"),
                Component.translatable("description.ravenousmaw.oxytocin_stacks"),
                Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "textures/codex/oxytocinergic_esca.png"),
                player.getData(RavenousAttachments.OXYTOCIN_STACKS),
                Constants.OXYTOCINERGIC_ESCA_MAX_STACKS
            ));
        }

        return stats;
    }

    public ItemStack getStack() {
        return stack;
    }

    public Level getLevel() {
        return level;
    }
}
