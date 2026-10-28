package net.rywir.ravenousmaw.system;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousItems;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.util.HelperData;

import java.util.function.Supplier;

public class StageHandler {
    private final ItemStack stack;

    public StageHandler(ItemStack stack) {
        this.stack = stack;
    }

    public Stages getStage() {
        if (this.stack.is(RavenousItems.RAVENOUS_MAW_LATENT)) {
            return Stages.LATENT;
        }

        if (this.stack.is(RavenousItems.RAVENOUS_MAW_ADVANCED)) {
            return Stages.ADVANCED;
        }

        if (this.stack.is(RavenousItems.RAVENOUS_MAW_NOBLE)) {
            return Stages.NOBLE;
        }

        if (this.stack.is(RavenousItems.RAVENOUS_MAW_EXCELSIOR)) {
            return Stages.EXCELSIOR;
        }

        return null;
    }

    /**
     * Slot of the Maw that can feast on {@code boss}, i.e. whose stage objective is that boss, or -1 if the
     * inventory has none. The held slot wins, then the first match in slot order.
     */
    public static int findMawSlotFor(Inventory inventory, EntityType<?> boss) {
        if (isMawFor(inventory.getItem(inventory.selected), boss)) {
            return inventory.selected;
        }

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isMawFor(inventory.getItem(slot), boss)) {
                return slot;
            }
        }

        return -1;
    }

    private static boolean isMawFor(ItemStack candidate, EntityType<?> boss) {
        if (!candidate.is(RavenousItemTagsProvider.MAW)) return false;

        Stages stage = new StageHandler(candidate).getStage();

        return stage != null && stage.getObjective() == boss;
    }

    public ItemStack stageUp(int currentStage, ItemStack source, HolderLookup.Provider registries) {
        if (currentStage >= 3) {
            return source;
        }

        Stages newStage = Stages.byId(currentStage + 1);
        ItemStack newMaw = HelperData.getMawItem(newStage).getDefaultInstance();

        MutationHandler handler = new MutationHandler(source);

        handler.syncMutations(newMaw);
        transferCustomItemData(newMaw);

        for (Mutations mutation : Mutations.values()) {
            mutation.ability().onUpdate(newMaw, registries);
        }

        return newMaw;
    }

    public void transferCustomItemData(ItemStack target) {
        if (target.isEmpty()) {
            return;
        }

        Component name = stack.get(DataComponents.CUSTOM_NAME);

        if (name != null) {
            target.set(DataComponents.CUSTOM_NAME, name);
        }

        ItemLore lore = stack.get(DataComponents.LORE);

        if (lore != null) {
            target.set(DataComponents.LORE, lore);
        }

        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);

        if (enchantments != null) {
            target.set(DataComponents.ENCHANTMENTS, enchantments);
        }

        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);

        if (stored != null) {
            target.set(DataComponents.STORED_ENCHANTMENTS, stored);
        }

        int repairCost = stack.getOrDefault(DataComponents.REPAIR_COST, 0);

        if (repairCost > 0) {
            target.set(DataComponents.REPAIR_COST, repairCost);
        }

        int damageVal = stack.getDamageValue();

        target.setDamageValue(damageVal);

        // Mutation state components are only created by each ability's onCraft, which stageUp never calls.
        // Copy them when present so energy, kill count and the charge flag survive the stage-up.
        copyComponent(target, DataComponentTypes.CHARGING_SOUND_TYPE);
        copyComponent(target, DataComponentTypes.STORED_ENERGY);
        copyComponent(target, DataComponentTypes.VORACITY_COMPONENT_TYPE);
    }

    private <T> void copyComponent(ItemStack target, Supplier<DataComponentType<T>> type) {
        T value = stack.get(type);

        if (value != null) {
            target.set(type, value);
        }
    }
}
