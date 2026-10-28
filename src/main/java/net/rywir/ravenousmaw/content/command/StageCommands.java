package net.rywir.ravenousmaw.content.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.system.StageHandler;

/** {@code /ravenousmaw stage ...}: changes the stage of the Maw in the main hand. */
public class StageCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("stage")
            .then(Commands.literal("up")
                .executes(StageCommands::up)
            );
    }

    private static int up(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = context.getSource().getPlayerOrException();
        ItemStack stack = CommandHelper.heldMaw(player);
        if (stack == null) return 0;

        StageHandler handler = new StageHandler(stack);
        int currentStage = handler.getStage().getId();
        ItemStack newMaw = handler.stageUp(currentStage, stack, player.level().registryAccess());

        player.setItemSlot(EquipmentSlot.MAINHAND, newMaw);

        return 1;
    }
}
