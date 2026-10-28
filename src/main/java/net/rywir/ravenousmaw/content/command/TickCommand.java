package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.rywir.ravenousmaw.content.attachment.MawTickData;
import net.rywir.ravenousmaw.registry.RavenousAttachments;
import net.rywir.ravenousmaw.util.Constants;

/**
 * Debug command: sets the per-player Maw tick counter so the 9-minute instability and the
 * 3-minute dialogue can be tested without waiting. Example: {@code tick 10760} fires the instability in 2 s.
 */
public class TickCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ravenousmaw")
                .then(Commands.literal("tick")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("ticks", IntegerArgumentType.integer(0, Constants.MAW_INSTABILITY_TICKS))
                        .executes(TickCommand::execute)
                    )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int ticks = IntegerArgumentType.getInteger(context, "ticks");

        MawTickData tickData = player.getData(RavenousAttachments.MAW_TICK_DATA);
        tickData.setTickCounter(ticks);

        player.sendSystemMessage(Component.translatable("message.ravenousmaw.tick_set", ticks, Constants.MAW_INSTABILITY_TICKS));
        return 1;
    }
}
