package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;
import net.rywir.ravenousmaw.util.Constants;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElectricMending implements IMutationAbility {
    private static final Component LOW_ENERGY_WARNING = Component.translatable("ravenousmaw.electric_mending_low_energy");

    /** Game time of the last low energy warning per player. */
    private static final Map<UUID, Long> LAST_WARNING_TICK = new ConcurrentHashMap<>();

    @Override
    public void onCraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.set(DataComponentTypes.STORED_ENERGY, 0);
    }

    @Override
    public void decraft(ItemStack stack, HolderLookup.Provider registries) {
        stack.remove(DataComponentTypes.STORED_ENERGY);
    }

    /**
     * Whether the Maw holds enough FE for a full-power hit. Below it, the hit is weakened
     * (see {@link #getHitDamageMultiplier}).
     */
    public boolean canPowerHit(ItemStack stack) {
        var energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);

        return energy != null && energy.getEnergyStored() >= Constants.ELECTRIC_MENDING_ATTACK_ENERGY_COST;
    }

    /** Multiplier for the whole melee hit: 1 with enough FE, a heavy penalty without. */
    public float getHitDamageMultiplier(MutationHandler handler, ItemStack stack) {
        if (!handler.has(Mutations.ELECTRIC_MENDING) || canPowerHit(stack)) return 1.0F;

        return Constants.ELECTRIC_MENDING_LOW_ENERGY_DAMAGE_MULTIPLIER;
    }

    /**
     * Tells the player why a hit was weakened. Server side, called when the battery could not pay for the hit.
     * Rate limited per player so a fight does not repeat it on every swing.
     */
    public void warnLowEnergy(ServerPlayer player) {
        long now = player.level().getGameTime();
        Long last = LAST_WARNING_TICK.get(player.getUUID());

        if (last != null && now - last < Constants.ELECTRIC_MENDING_WARNING_COOLDOWN_TICKS) {
            return;
        }

        LAST_WARNING_TICK.put(player.getUUID(), now);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.displayClientMessage(LOW_ENERGY_WARNING, true);
    }

    /** Each melee hit that lands costs FE. A battery below the cost is kept for mending and the magnet. */
    @Override
    public void onAttack(ItemStack stack, LivingEntity target, Level level) {
        if (level.isClientSide()) return;

        if (!new MutationHandler(stack).has(Mutations.ELECTRIC_MENDING)) return;
        if (!canPowerHit(stack)) return;

        stack.getCapability(Capabilities.EnergyStorage.ITEM).extractEnergy(Constants.ELECTRIC_MENDING_ATTACK_ENERGY_COST, false);
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
