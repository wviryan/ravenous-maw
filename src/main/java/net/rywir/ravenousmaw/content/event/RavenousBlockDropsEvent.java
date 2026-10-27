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

import java.util.Random;

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

        int fortuneLevel = enchantmentHandler.getEnchLevel(Enchantments.FORTUNE, level);

        Random random = new Random();
        int multiplier = random.nextInt(fortuneLevel + 1);

        ItemEntity drop = event.getDrops().getFirst();

        for (int i = 0; i < multiplier + 1; i++) {
            ItemEntity duplicate = new ItemEntity(
                level,
                event.getPos().getX(),
                event.getPos().getY(),
                event.getPos().getZ(),
                drop.getItem().copy()
            );
            level.addFreshEntity(duplicate);
        }
    }
}
