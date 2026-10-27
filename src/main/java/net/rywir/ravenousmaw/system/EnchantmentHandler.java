package net.rywir.ravenousmaw.system;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantmentHandler {
    private ItemStack stack;

    public EnchantmentHandler(ItemStack stack) {
        this.stack = stack;
    }

    public boolean hasMaxedOut(ResourceKey<Enchantment> key, HolderLookup.Provider registries) {
        var registry = registries.lookupOrThrow(Registries.ENCHANTMENT);
        var ench = registry.getOrThrow(key);

        int enchExistingLevel = stack.getEnchantmentLevel(ench);
        int enchMaxLevel = ench.value().getMaxLevel();

        boolean hasEnchantment = enchExistingLevel > 0;

        if (!hasEnchantment) {
            return false;
        }

        boolean exceeds = enchMaxLevel <= enchExistingLevel;

        if (exceeds) {
            return true;
        } else {
            return false;
        }
    }

    public int getEnchLevel(ResourceKey<Enchantment> key, HolderLookup.Provider registries) {
        var registry = registries.lookupOrThrow(Registries.ENCHANTMENT);
        var ench = registry.getOrThrow(key);

        return stack.getEnchantmentLevel(ench);
    }

    public void with(ResourceKey<Enchantment> key, int enchLevel, HolderLookup.Provider registries) {
        var registry = registries.lookupOrThrow(Registries.ENCHANTMENT);
        var ench = registry.getOrThrow(key);
        stack.enchant(ench, enchLevel);
    }

    public void without(ResourceKey<Enchantment> key, HolderLookup.Provider registries) {
        var registry = registries.lookupOrThrow(Registries.ENCHANTMENT);
        var holder = registry.getOrThrow(key);

        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            mutable.set(holder, 0);
        });
    }
}
