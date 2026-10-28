package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;

/** {@code /ravenousmaw energy ...}: charges the Electric Mending storage of the Maw in the main hand. */
public class EnergyCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("energy")
            .then(Commands.literal("charge")
                .executes(context -> charge(context, false))
            )
            .then(Commands.literal("surcharge")
                .executes(context -> charge(context, true))
            );
    }

    /**
     * The storage caps how much one call accepts. A plain charge makes a single call on purpose, so repeated
     * runs test half-filled states; a surcharge keeps feeding it until it is full.
     */
    private static int charge(CommandContext<CommandSourceStack> context, boolean untilFull) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ItemStack stack = CommandHelper.heldMaw(player);
        if (stack == null) return 0;

        if (!new MutationHandler(stack).has(Mutations.ELECTRIC_MENDING)) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.no_electric_mending"));
            return 0;
        }

        var energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);

        if (energy == null) return 0;

        if (energy.getEnergyStored() >= energy.getMaxEnergyStored()) {
            player.sendSystemMessage(Component.translatable("message.ravenousmaw.already_charged"));
            return 0;
        }

        int total = 0;
        int received;
        do {
            received = energy.receiveEnergy(energy.getMaxEnergyStored(), false);
            total += received;
        } while (untilFull && received > 0);

        player.sendSystemMessage(Component.translatable("message.ravenousmaw.charged", total,
            energy.getEnergyStored(), energy.getMaxEnergyStored()));

        return 1;
    }
}
