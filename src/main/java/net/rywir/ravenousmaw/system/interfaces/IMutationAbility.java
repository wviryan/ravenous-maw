package net.rywir.ravenousmaw.system.interfaces;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.AbilityDispatcher;
import net.rywir.ravenousmaw.system.MutationHandler;

public interface IMutationAbility {
    default void onMine() {}

    default float getAttackDamageBonus(MutationHandler handler, ItemStack stack, LivingEntity target, Level level, Stages stage) {
        return 0.0F;
    }

    default void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged, MutationHandler handler, Stages stage, AbilityDispatcher dispatcher) {}

    default InteractionResultHolder<ItemStack> use(MutationHandler mutationHandler, ItemStack stack, Level level, Player player, InteractionHand hand) { return InteractionResultHolder.pass(stack); }

    default void onAttack(ItemStack stack, LivingEntity target, Level level) {}

    default boolean interactLivingEntity(ItemStack stack, Player player, Entity entity) { return false; }

    default void onCraft(ItemStack stack) {}

    default void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {}

    default void onUpdate(ItemStack stack, Level level) {}

    default void decraft(ItemStack stack) {}

    default boolean isMutatable(ItemStack stack, Level level) { return true; }

    default void stealLife(ItemStack maw, float damage, Player player) {}
}
