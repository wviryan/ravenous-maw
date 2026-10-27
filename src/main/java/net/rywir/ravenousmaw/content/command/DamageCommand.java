package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.AbilityDispatcher;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;

/**
 * Debug command: reports the held Maw's target-independent damage bonus (Arcane Hypertrophy's Sharpness,
 * Reckless Devourer's multiplier and the resulting total) so they can be read without hitting a mob.
 * Target-dependent bonuses (Combustive Enzyme, Resonant Rending, Insatiable Voracity) are not included.
 */
public class DamageCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ravenousmaw")
                .then(Commands.literal("damage")
                    .requires(source -> source.hasPermission(2))
                    .executes(DamageCommand::execute)
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

        Stages stage = new StageHandler(stack).getStage();

        if (stage == null) {
            player.sendSystemMessage(Component.translatable("warning.ravenousmaw.no_maw"));
            return 0;
        }

        MutationHandler handler = new MutationHandler(stack);
        AbilityDispatcher dispatcher = new AbilityDispatcher();

        float arcane = dispatcher.getTargetIndependentBonus(handler, stack, player.level(), stage);
        float multiplier = dispatcher.getDamageMultiplier(handler);

        // Same rule as the real hit: Reckless multiplies the bonus too.
        player.sendSystemMessage(Component.translatable("message.ravenousmaw.damage", arcane, multiplier, arcane * multiplier));
        return 1;
    }
}
