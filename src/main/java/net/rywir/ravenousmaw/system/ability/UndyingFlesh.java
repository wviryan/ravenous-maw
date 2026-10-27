package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;

public class UndyingFlesh implements IMutationAbility {
    private static final int DURATION = 80;
    private static final int AMPLIFIER = 5;

    @Override
    public void onCraft(ItemStack stack, HolderLookup.Provider registries) {
        onUpdate(stack, registries);
    }

    @Override
    public void onUpdate(ItemStack stack, HolderLookup.Provider registries) {
        MutationHandler handler = new MutationHandler(stack);

        if (!handler.has(Mutations.UNDYING_FLESH)) {
            return;
        }

        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }

    @Override
    public void decraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.remove(DataComponents.UNBREAKABLE);
    }

    @Override
    public void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {
        if (player.level().isClientSide()) return;

        boolean hasUndyingFlesh = mutationHandler.has(Mutations.UNDYING_FLESH);

        if (!hasUndyingFlesh) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, DURATION, AMPLIFIER));
        player.displayClientMessage(Component.translatable("instability_message.ravenousmaw.undying_flesh"), true);
    }
}