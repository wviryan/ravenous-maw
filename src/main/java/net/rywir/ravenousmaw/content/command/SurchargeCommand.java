package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;

public class SurchargeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ravenousmaw")
                .then(Commands.literal("surcharge")
                    .requires(source -> source.hasPermission(2))
                    .executes(SurchargeCommand::execute)
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();

        if (!stack.is(RavenousItemTagsProvider.MAW)) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.no_maw"));
            return 0;
        }

        MutationHandler handler = new MutationHandler(stack);

        if (!handler.has(Mutations.ELECTRIC_MENDING)) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.no_electric_mending"));
            return 0;
        }

        var energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);

        if (energy == null) return 0;

        if (energy.getEnergyStored() >= energy.getMaxEnergyStored()) {
            player.sendSystemMessage(Component.translatable("message.ravenousmaw.already_charged"));
            return 0;
        }

        // The storage caps how much one call accepts, so keep feeding it until it is full.
        int total = 0;
        int received;
        do {
            received = energy.receiveEnergy(energy.getMaxEnergyStored(), false);
            total += received;
        } while (received > 0);

        player.sendSystemMessage(Component.translatable("message.ravenousmaw.charged", total,
            energy.getEnergyStored(), energy.getMaxEnergyStored()));

        return 1;
    }
}
