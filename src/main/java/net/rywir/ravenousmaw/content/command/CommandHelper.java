package net.rywir.ravenousmaw.content.command;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.Mutations;
import org.jetbrains.annotations.Nullable;

/**
 * Checks shared by the /ravenousmaw subcommands. Each one tells the player what went wrong and returns null,
 * so a command only has to stop when it gets null.
 */
public class CommandHelper {
    /** The Maw in the player's main hand, or null (after a warning) if the hand holds something else. */
    public static @Nullable ItemStack heldMaw(Player player) {
        ItemStack stack = player.getMainHandItem();

        if (!stack.is(RavenousItemTagsProvider.MAW)) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.no_maw"));
            return null;
        }

        return stack;
    }

    public static @Nullable Mutations mutation(Player player, String key) {
        Mutations mutation = Mutations.byKey(key);

        if (mutation == null) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.unknown_mutation", key));
        }

        return mutation;
    }

    public static @Nullable Assimilations assimilation(Player player, String key) {
        Assimilations assimilation = Assimilations.byKey(key);

        if (assimilation == null) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.unknown_assimilation", key));
        }

        return assimilation;
    }
}
