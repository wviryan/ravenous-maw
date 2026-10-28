package net.rywir.ravenousmaw.system;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the detail lines of the Maw tooltip. Plain text only, so it is safe in common code;
 * the item decides when to show it (see MawItem#appendHoverText).
 */
public final class MawTooltip {
    private MawTooltip() {}

    /**
     * Every mutation grouped by the stage that offers it. Mutations the Maw has are plain,
     * the others are dimmed so the player can see what is still missing. Settings and counters belong to the config screen.
     */
    public static List<Component> mutationLines(ItemStack stack) {
        MutationHandler handler = new MutationHandler(stack);
        List<Component> lines = new ArrayList<>();

        lines.add(Component.translatable("maw.ravenousmaw.mutation_string").append(": " + handler.matchMutations().size() + "/" + Mutations.values().length).withStyle(ChatFormatting.YELLOW));

        for (Stages stage : Stages.values()) {
            lines.add(stage.getDisplayComponent().copy().withStyle(ChatFormatting.GOLD));

            for (Mutations mutation : Mutations.values()) {
                if (mutation.stage() != stage) continue;

                Component name = Component.translatable("mutation.ravenousmaw." + mutation.key());

                if (!handler.has(mutation)) {
                    lines.add(Component.literal("✗ ").append(name).withStyle(ChatFormatting.DARK_GRAY));
                    continue;
                }

                lines.add(Component.literal("・").append(name).withStyle(ChatFormatting.GRAY));
            }
        }

        return lines;
    }
}
