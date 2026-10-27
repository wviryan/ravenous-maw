package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;

public class DestabilizeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ravenousmaw")
                .then(Commands.literal("destabilize")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("mutation", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for (var mut : Mutations.values()) {
                                builder.suggest(mut.key());
                            }
                            return builder.buildFuture();
                        })
                        .executes(DestabilizeCommand::execute)
                    )
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

        String key = StringArgumentType.getString(context, "mutation");

        Mutations target = Mutations.byKey(key);

        if (target == null) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.unknown_mutation", key));
            return 0;
        }

        MutationHandler handler = new MutationHandler(stack);

        target.ability().onInstability(handler, stack, player);

        return 1;
    }
}
