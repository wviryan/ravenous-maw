package net.rywir.ravenousmaw.system.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.EnchantmentHandler;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;

public class ArcaneHypertrophy implements IMutationAbility {
    // look into RavenousLivingDropsEvent for Looting III
    // look into RavenousBlockDropsEvent for Fortune III (duplicates every drop, skipped with Silk Touch)

    // Sharpness V
    @Override
    public float getAttackDamageBonus(MutationHandler handler, ItemStack stack, LivingEntity target, Level level, Stages stage) {
        boolean hasArcaneHypertrophy = handler.has(Mutations.ARCANE_HYPERTROPHY);

        if (!hasArcaneHypertrophy) {
            return 0;
        }

        EnchantmentHandler enchantmentHandler = new EnchantmentHandler(stack);

        boolean hasMaxedOutSharpness = enchantmentHandler.hasMaxedOut(Enchantments.SHARPNESS, level);

        if (!hasMaxedOutSharpness) {
            return 0;
        }
        
        int enchantmentLevel = enchantmentHandler.getEnchLevel(Enchantments.SHARPNESS, level);

        return 0.5f * ((float) enchantmentLevel) + 0.5f;
    }
}
