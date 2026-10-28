package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.system.AssimilationHandler;

/**
 * {@code /ravenousmaw assimilation ...}: grants or revokes assimilations without the deed.
 * They live on the player, not on the Maw, so these act on you. Granting goes through
 * {@link AssimilationHandler#grant} like the deeds do, so a carried Maw of the assimilation's stage or higher
 * is still required.
 */
public class AssimilationCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("assimilation")
            .then(Commands.literal("add")
                .then(Commands.literal("all")
                    .executes(AssimilationCommands::addAll)
                )
                .then(Commands.argument("assimilation", StringArgumentType.string())
                    .suggests(AssimilationSuggestions.PROVIDER)
                    .executes(AssimilationCommands::add)
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument("assimilation", StringArgumentType.string())
                    .suggests(AssimilationSuggestions.PROVIDER)
                    .executes(AssimilationCommands::remove)
                )
            );
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        Assimilations target = CommandHelper.assimilation(player, StringArgumentType.getString(context, "assimilation"));
        if (target == null) return 0;

        if (AssimilationHandler.has(player, target)) {
            player.sendSystemMessage(Component.translatable("message.ravenousmaw.already_assimilated", target.title()));
            return 0;
        }

        if (!AssimilationHandler.carriesMawOfStage(player, target.getStage())) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.assimilation_stage", target.title(), target.getStage().getDisplayComponent()));
            return 0;
        }

        AssimilationHandler.grant(player, target);

        return 1;
    }

    /** Grants every assimilation the carried Maws allow; the rest are skipped. */
    private static int addAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int granted = 0;

        for (Assimilations assimilation : Assimilations.values()) {
            if (AssimilationHandler.has(player, assimilation)) continue;

            AssimilationHandler.grant(player, assimilation);

            if (AssimilationHandler.has(player, assimilation)) granted++;
        }

        if (granted == 0) {
            player.sendSystemMessage(Component.translatable("message.ravenousmaw.nothing_assimilated"));
        }

        return granted;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        Assimilations target = CommandHelper.assimilation(player, StringArgumentType.getString(context, "assimilation"));
        if (target == null) return 0;

        if (!AssimilationHandler.revoke(player, target)) {
            player.sendSystemMessage(Component.translatable("message.ravenousmaw.not_assimilated", target.title()));
            return 0;
        }

        return 1;
    }
}
