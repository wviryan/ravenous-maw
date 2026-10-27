package net.rywir.ravenousmaw.content.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.EnchantmentHandler;
import net.rywir.ravenousmaw.system.MutationHandler;

import java.util.List;

public class RavenousBlockDropsEvent {
    @SubscribeEvent
    public static void onBlockDropsEvent(BlockDropsEvent event) {
        Entity entity = event.getBreaker();

        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        boolean isMaw = stack.is(RavenousItemTagsProvider.MAW);

        if (!isMaw) {
            return;
        }

        MutationHandler mutationHandler = new MutationHandler(stack);

        boolean hasArcaneHypertrophy = mutationHandler.has(Mutations.ARCANE_HYPERTROPHY);

        if (!hasArcaneHypertrophy) {
            return;
        }

        Level level = player.level();
        EnchantmentHandler enchantmentHandler = new EnchantmentHandler(stack);

        boolean isFortuneMaxed = enchantmentHandler.hasMaxedOut(Enchantments.FORTUNE, level);

        if (!isFortuneMaxed) {
            return;
        }

        BlockState state = event.getState();

        boolean isOre = state.is(Tags.Blocks.ORES);

        if (!isOre) {
            return;
        }

        // Silk Touch drops the ore block itself, duplicating it would be a free ore farm
        boolean hasSilkTouch = enchantmentHandler.getEnchLevel(Enchantments.SILK_TOUCH, level) > 0;

        if (hasSilkTouch) {
            return;
        }

        // Some loot tables or mods leave nothing to duplicate
        if (event.getDrops().isEmpty()) {
            return;
        }

        int fortuneLevel = enchantmentHandler.getEnchLevel(Enchantments.FORTUNE, level);

        // 0 to fortuneLevel extra copies of every drop, so Fortune III gives 1x to 4x the vanilla amount
        int extraCopies = level.random.nextInt(fortuneLevel + 1);

        // Snapshot, because the duplicates are added to the same list
        List<ItemEntity> originals = List.copyOf(event.getDrops());

        for (int i = 0; i < extraCopies; i++) {
            for (ItemEntity original : originals) {
                // Same position and motion as the original drop (block centre, vanilla scatter)
                ItemEntity duplicate = new ItemEntity(
                    level,
                    original.getX(),
                    original.getY(),
                    original.getZ(),
                    original.getItem().copy(),
                    original.getDeltaMovement().x,
                    original.getDeltaMovement().y,
                    original.getDeltaMovement().z
                );
                event.getDrops().add(duplicate);
            }
        }
    }
}
