package net.rywir.ravenousmaw.system;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;

public class AbilityDispatcher implements IMutationAbility {
    @Override
    public void onAttack(ItemStack stack, LivingEntity target, Level level) {
        Mutations.COMBUSTIVE_ENZYME.ability().onAttack(stack, target, level);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(MutationHandler mutationHandler, ItemStack stack, Level level, Player player, InteractionHand hand) {
        boolean hasIrisOut = mutationHandler.has(Mutations.IRIS_OUT);
        int isActive = mutationHandler.getConfigVal(Mutations.Parameters.LIVING_PROJECTILE);

        // Both are required, like MawItem.getUseDuration: with the toggle off there is no charge to draw.
        if (hasIrisOut && isActive == 1) {
            return Mutations.IRIS_OUT.ability().use(mutationHandler, stack, level, player, hand);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged, MutationHandler handler, Stages stage, AbilityDispatcher dispatcher) {
        Mutations.IRIS_OUT.ability().releaseUsing(stack, level, entity, timeCharged, handler, stage, dispatcher);
    }

    @Override
    public boolean interactLivingEntity(ItemStack stack, Player player, Entity entity) {
        boolean result = Mutations.MENTAL_SUPREMACY.ability().interactLivingEntity(stack, player, entity);
        return result;
    }

    @Override
    public float getAttackDamageBonus(MutationHandler handler, ItemStack stack, LivingEntity target, Level level, Stages stage) {
        float combustionBonus = Mutations.COMBUSTIVE_ENZYME.ability().getAttackDamageBonus(handler, stack, target, level, stage);
        float rendingBonus = Mutations.RESONANT_RENDING.ability().getAttackDamageBonus(handler, stack, target, level, stage);
        float voracityBonus = Mutations.INSATIABLE_VORACITY.ability().getAttackDamageBonus(handler, stack, target, level, stage);
        float adaptiveMultiplier = Mutations.ADAPTIVE_SHIFT.ability().getAttackDamageBonus(handler, stack, target, level, stage);
        float arcaneBonus = Mutations.ARCANE_HYPERTROPHY.ability().getAttackDamageBonus(handler, stack, target, level, stage);

        // Reckless Devourer's multiplier (Adaptive Shift) applies to every bonus, including Arcane Hypertrophy's Sharpness.
        float bonus = (combustionBonus + rendingBonus + voracityBonus + arcaneBonus) * adaptiveMultiplier;

        return bonus;
    }

    @Override
    public void onMine() {}

    @Override
    public void onCraft(ItemStack stack, HolderLookup.Provider registries) {}

    @Override
    public void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {}
}
