package net.rywir.ravenousmaw.content.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

public class CodexItem extends Item {
    public CodexItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            return InteractionResult.FAIL;
        }

        PatchouliAPI.get().openBookGUI(Identifier.fromNamespaceAndPath("ravenousmaw", "mutation_codex"));
        return InteractionResult.SUCCESS;
    }
}
