package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * The single {@code /ravenousmaw} root. Every subcommand is a developer tool, so the permission is set once here,
 * and each group (mutation, assimilation, energy, stage, debug) lives in its own class.
 */
public class RavenousCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ravenousmaw")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(MutationCommands.build())
                .then(AssimilationCommands.build())
                .then(EnergyCommands.build())
                .then(StageCommands.build())
                .then(DebugCommands.build())
        );
    }
}
