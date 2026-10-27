package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;

public class ElectricMending implements IMutationAbility {
    @Override
    public void onCraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.set(DataComponentTypes.STORED_ENERGY, 0);
    }

    @Override
    public void decraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.remove(DataComponentTypes.STORED_ENERGY);
    }

    @Override
    public void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {
        if (!player.isInWater()) return;

        Holder<DamageType> type = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.PLAYER_ATTACK);

        DamageSource source = new DamageSource(type, player, player, player.position());

        player.hurt(source, 8.0F);
        player.displayClientMessage(Component.translatable("instability_message.ravenousmaw.electric_mending"), true);
    }
}
