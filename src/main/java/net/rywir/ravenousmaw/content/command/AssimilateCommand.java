package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.system.AssimilationHandler;

import java.util.Locale;

/**
 * {@code /ravenousmaw assimilate <assimilation>}: grants an assimilation without the deed.
 * Assimilations live on the player, so it goes through {@link AssimilationHandler#grant} like the deeds do,
 * which means a carried Maw of the assimilation's stage or higher is still required.
 */
public class AssimilateCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ravenousmaw")
                .then(Commands.literal("assimilate")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("assimilation", StringArgumentType.string())
                        .suggests(AssimilationSuggestions.PROVIDER)
                        .executes(AssimilateCommand::execute)
                    )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String key = StringArgumentType.getString(context, "assimilation");

        Assimilations target = Assimilations.byKey(key);

        if (target == null) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.unknown_assimilation", key));
            return 0;
        }

        if (AssimilationHandler.has(player, target)) {
            player.sendSystemMessage(Component.translatable("message.ravenousmaw.already_assimilated", target.title()));
            return 0;
        }

        if (!AssimilationHandler.carriesMawOfStage(player, target.getStage())) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.assimilation_stage", target.title(), Component.translatable("stage.ravenousmaw." + target.getStage().name().toLowerCase(Locale.ROOT))));
            return 0;
        }

        AssimilationHandler.grant(player, target);

        return 1;
    }
}
