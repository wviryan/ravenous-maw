package net.rywir.ravenousmaw.system.interfaces;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

    /** Multiplier applied to the whole hit (base damage + bonuses). 1.0 means unchanged. */
    default float getDamageMultiplier(MutationHandler handler) {
        return 1.0F;
    }

    default void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged, MutationHandler handler, Stages stage, AbilityDispatcher dispatcher) {}

    default InteractionResult use(MutationHandler mutationHandler, ItemStack stack, Level level, Player player, InteractionHand hand) { return InteractionResult.PASS; }

    default void onAttack(ItemStack stack, LivingEntity target, Level level) {}

    default boolean interactLivingEntity(ItemStack stack, Player player, Entity entity) { return false; }

    /** Called once when the mutation is added to the Maw. Sets up the components and modifiers it needs. */
    default void onCraft(ItemStack stack, HolderLookup.Provider registries) {}

    default void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {}

    /**
     * Re-applies the mutation to the Maw after a parameter change or a stage-up.
     * Must be idempotent, since it runs many times over the life of an item.
     */
    default void onUpdate(ItemStack stack, HolderLookup.Provider registries) {}

    /** Called when the mutation is removed from the Maw. Undoes everything onCraft and onUpdate applied. */
    default void decraft(ItemStack stack, HolderLookup.Provider registries) {}

    default boolean isMutatable(ItemStack stack, Level level) { return true; }

    default void stealLife(ItemStack maw, float damage, Player player) {}
}
