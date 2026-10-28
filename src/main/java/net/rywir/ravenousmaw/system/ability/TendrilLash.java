package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;
import net.rywir.ravenousmaw.util.Constants;

public class TendrilLash implements IMutationAbility {
    private static final Identifier BLOCK_RANGE_ID = Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "tendril_lash_block_range");
    private static final Identifier ENTITY_RANGE_ID = Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "tendril_lash_entity_range");

    // Early versions used vanilla's base ids, which also stacked on every update. Clean those up on existing items.
    private static final Identifier LEGACY_BLOCK_RANGE_ID = Identifier.withDefaultNamespace("base_block_interaction_range");
    private static final Identifier LEGACY_ENTITY_RANGE_ID = Identifier.withDefaultNamespace("base_entity_interaction_range");

    @Override
    public void onCraft(ItemStack stack, HolderLookup.Provider registries) {
        onUpdate(stack, registries);
    }

    @Override
    public void decraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, withoutReachModifiers(stack).build());
    }

    /** Rebuilds the reach modifiers from the current stage. Idempotent: old reach modifiers are replaced, never stacked. */
    @Override
    public void onUpdate(ItemStack stack, HolderLookup.Provider registries) {
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

        ItemAttributeModifiers.Builder builder = withoutReachModifiers(stack);

        if (extraRange > 0) {
            builder.add(
                Attributes.BLOCK_INTERACTION_RANGE,
                new AttributeModifier(BLOCK_RANGE_ID, extraRange, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            );

            builder.add(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(ENTITY_RANGE_ID, extraRange, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            );
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    /** A builder holding every attribute modifier of the stack except the reach ones this mutation owns. */
    private static ItemAttributeModifiers.Builder withoutReachModifiers(ItemStack stack) {
        ItemAttributeModifiers existingModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        existingModifiers.modifiers().forEach(entry -> {
            Identifier id = entry.modifier().id();

            boolean isReachModifier = id.equals(BLOCK_RANGE_ID) || id.equals(ENTITY_RANGE_ID)
                || id.equals(LEGACY_BLOCK_RANGE_ID) || id.equals(LEGACY_ENTITY_RANGE_ID);

            if (!isReachModifier) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        });

        return builder;
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

        player.sendOverlayMessage(Component.translatable("instability_message.ravenousmaw.tendril_lash"));
    }
}
