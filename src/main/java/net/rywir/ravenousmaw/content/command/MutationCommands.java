package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;

import java.util.Arrays;
import java.util.List;

/** {@code /ravenousmaw mutation ...}: edits the mutations of the Maw in the main hand. */
public class MutationCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("mutation")
            .then(Commands.literal("add")
                .then(Commands.literal("all")
                    .executes(MutationCommands::addAll)
                )
                .then(Commands.argument("mutation", StringArgumentType.string())
                    .suggests(MutationSuggestions.PROVIDER)
                    .executes(MutationCommands::add)
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument("mutation", StringArgumentType.string())
                    .suggests(MutationSuggestions.PROVIDER)
                    .executes(MutationCommands::remove)
                )
            )
            .then(Commands.literal("destabilize")
                .then(Commands.argument("mutation", StringArgumentType.string())
                    .suggests(MutationSuggestions.PROVIDER)
                    .executes(MutationCommands::destabilize)
                )
            )
            .then(Commands.literal("possible")
                .executes(MutationCommands::possible)
            );
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ItemStack stack = CommandHelper.heldMaw(player);
        if (stack == null) return 0;

        Mutations target = CommandHelper.mutation(player, StringArgumentType.getString(context, "mutation"));
        if (target == null) return 0;

        new MutationHandler(stack).addWithCraft(target, player.level().registryAccess());

        return 1;
    }

    private static int addAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ItemStack stack = CommandHelper.heldMaw(player);
        if (stack == null) return 0;

        MutationHandler handler = new MutationHandler(stack);

        for (Mutations mutation : Mutations.values()) {
            handler.addWithCraft(mutation, player.level().registryAccess());
        }

        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ItemStack stack = CommandHelper.heldMaw(player);
        if (stack == null) return 0;

        Mutations target = CommandHelper.mutation(player, StringArgumentType.getString(context, "mutation"));
        if (target == null) return 0;

        new MutationHandler(stack).remove(target, player.level().registryAccess());

        return 1;
    }

    private static int destabilize(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ItemStack stack = CommandHelper.heldMaw(player);
        if (stack == null) return 0;

        Mutations target = CommandHelper.mutation(player, StringArgumentType.getString(context, "mutation"));
        if (target == null) return 0;

        target.ability().onInstability(new MutationHandler(stack), stack, player);

        return 1;
    }

    /**
     * Lists the mutations the held Maw does not have yet but could take at its stage, which is the same rule
     * the Mutation Matrix applies: a mutation of the Maw's stage or lower.
     */
    private static int possible(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ItemStack stack = CommandHelper.heldMaw(player);
        if (stack == null) return 0;

        Stages stage = new StageHandler(stack).getStage();
        MutationHandler handler = new MutationHandler(stack);

        List<Mutations> missing = Arrays.stream(Mutations.values())
            .filter(mutation -> mutation.stage().getId() <= stage.getId() && !handler.has(mutation))
            .toList();

        if (missing.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.ravenousmaw.no_possible_mutations"));
            return 0;
        }

        player.sendSystemMessage(Component.translatable("message.ravenousmaw.possible_mutations", missing.size(), stage.getDisplayComponent()));

        for (Mutations mutation : missing) {
            player.sendSystemMessage(Component.literal("- " + mutation.title() + " (" + mutation.key() + ", ").append(mutation.stage().getDisplayComponent()).append(")"));
        }

        return missing.size();
    }
}
