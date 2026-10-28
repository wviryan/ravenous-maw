package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.content.attachment.MawTickData;
import net.rywir.ravenousmaw.registry.RavenousAttachments;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.AbilityDispatcher;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.util.Constants;

/** {@code /ravenousmaw debug ...}: readouts and timers for testing without waiting or hitting mobs. */
public class DebugCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("debug")
            .then(Commands.literal("tick")
                .then(Commands.argument("ticks", IntegerArgumentType.integer(0, Constants.MAW_INSTABILITY_TICKS))
                    .executes(DebugCommands::tick)
                )
            )
            .then(Commands.literal("damage")
                .executes(DebugCommands::damage)
            );
    }

    /**
     * Sets the per-player Maw tick counter so the 9-minute instability and the 3-minute dialogue can be tested
     * without waiting. Example: {@code tick 10760} fires the instability in 2 s.
     */
    private static int tick(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int ticks = IntegerArgumentType.getInteger(context, "ticks");

        MawTickData tickData = player.getData(RavenousAttachments.MAW_TICK_DATA);
        tickData.setTickCounter(ticks);

        player.sendSystemMessage(Component.translatable("message.ravenousmaw.tick_set", ticks, Constants.MAW_INSTABILITY_TICKS));
        return 1;
    }

    /**
     * Reports the held Maw's target-independent damage bonus (Arcane Hypertrophy's Sharpness, Reckless Devourer's
     * multiplier and the resulting total) so they can be read without hitting a mob. Target-dependent bonuses
     * (Combustive Enzyme, Resonant Rending, Insatiable Voracity) are not included.
     */
    private static int damage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ItemStack stack = CommandHelper.heldMaw(player);
        if (stack == null) return 0;

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
