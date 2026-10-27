package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;
import net.rywir.ravenousmaw.util.Constants;

public class TendrilLash implements IMutationAbility {
    @Override
    public void onCraft(ItemStack stack) {
        onUpdate(stack, null);
    }

    @Override
    public void onUpdate(ItemStack stack, Level level) {
        MutationHandler handler = new MutationHandler(stack);

        if (!handler.has(Mutations.TENDRIL_LASH)) {
            return;
        }

        int extraRange = 0;

        StageHandler stageHandler = new StageHandler(stack);
        Stages stage = stageHandler.getStage();

        switch (stage) {
            case NOBLE:
                extraRange = Constants.TENDRIL_LASH_NOBLE_EXTRA_RANGE;
                break;
            case EXCELSIOR:
                extraRange = Constants.TENDRIL_LASH_EXCELSIOR_EXTRA_RANGE;
                break;
            default:
                break;
        }

        ItemAttributeModifiers existingModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        existingModifiers.modifiers().forEach(entry -> builder.add(entry.attribute(), entry.modifier(), entry.slot()));

        builder.add(
            Attributes.BLOCK_INTERACTION_RANGE,
            new AttributeModifier(
                ResourceLocation.withDefaultNamespace("base_block_interaction_range"),
                extraRange,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.MAINHAND
        );

        builder.add(
            Attributes.ENTITY_INTERACTION_RANGE,
            new AttributeModifier(
                ResourceLocation.withDefaultNamespace("base_entity_interaction_range"),
                extraRange,
                AttributeModifier.Operation.ADD_VALUE
            ),
            EquipmentSlotGroup.MAINHAND
        );

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    @Override
    public void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {
        if (player.level().isClientSide()) return;

        boolean hasTendrilLash = mutationHandler.has(Mutations.TENDRIL_LASH);

        if (!hasTendrilLash) {
            return;
        }

        ItemStack off = player.getItemBySlot(EquipmentSlot.OFFHAND);
        ItemStack main = player.getItemBySlot(EquipmentSlot.MAINHAND);

        player.setItemSlot(EquipmentSlot.MAINHAND, off);
        player.setItemSlot(EquipmentSlot.OFFHAND, main);

        player.displayClientMessage(Component.translatable("instability_message.ravenousmaw.tendril_lash"), true);
    }
}
