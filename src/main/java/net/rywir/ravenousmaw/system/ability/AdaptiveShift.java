package net.rywir.ravenousmaw.system.ability;


import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.EnchantmentHandler;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;

public class AdaptiveShift implements IMutationAbility {
    @Override
    public float getAttackDamageBonus(MutationHandler mutationHandler, ItemStack stack, LivingEntity target, Level level, Stages stage) {
        boolean hasShift = mutationHandler.has(Mutations.ADAPTIVE_SHIFT);

        float standard = 1.0F;
        float multiplier = 2.0F;

        if (!hasShift) {
            return standard;
        }

        int isDamageMultiplierActivated = mutationHandler.getConfigVal(Mutations.Parameters.RECKLESS_DEVOURER);

        if (isDamageMultiplierActivated == 1) {
            return multiplier;
        } else {
            return standard;
        }
    }

    @Override
    public void onCraft(ItemStack stack, HolderLookup.Provider registries) {
        // The default toggle state (Silky Fang off) must already give Fortune III, not only after the first toggle.
        onUpdate(stack, registries);
    }

    @Override
    public void decraft(ItemStack stack, HolderLookup.Provider registries) {
        EnchantmentHandler enchantmentHandler = new EnchantmentHandler(stack);

        enchantmentHandler.without(Enchantments.FORTUNE, registries);
        enchantmentHandler.without(Enchantments.SILK_TOUCH, registries);
    }

    @Override
    public void onUpdate(ItemStack stack, HolderLookup.Provider registries) {
        MutationHandler mutationHandler = new MutationHandler(stack);
        boolean hasShift = mutationHandler.has(Mutations.ADAPTIVE_SHIFT);

        if (!hasShift) {
            return;
        }

        int isSilkTouchActivated = mutationHandler.getConfigVal(Mutations.Parameters.SILKY_FANG);

        EnchantmentHandler enchantmentHandler = new EnchantmentHandler(stack);

        if (isSilkTouchActivated == 1) {
            enchantmentHandler.without(Enchantments.FORTUNE, registries);
            enchantmentHandler.with(Enchantments.SILK_TOUCH, 1, registries);
        } else {
            enchantmentHandler.without(Enchantments.SILK_TOUCH, registries);
            enchantmentHandler.with(Enchantments.FORTUNE, 3, registries);
        }
    }

    @Override
    public boolean isMutatable(ItemStack stack, Level level) {
        EnchantmentHandler handler = new EnchantmentHandler(stack);

        return handler.hasMaxedOut(Enchantments.SILK_TOUCH, level.registryAccess());
    }
}
