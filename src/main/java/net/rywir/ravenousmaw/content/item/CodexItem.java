package net.rywir.ravenousmaw.content.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

public class CodexItem extends Item {
    public CodexItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            return InteractionResultHolder.fail(stack);
        }

        PatchouliAPI.get().openBookGUI(Identifier.fromNamespaceAndPath("ravenousmaw", "mutation_codex"));
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
