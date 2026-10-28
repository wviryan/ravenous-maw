package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;
import net.rywir.ravenousmaw.util.Constants;

import java.util.Comparator;
import java.util.List;
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

    /** FE a plain hit costs: grows with the stage. */
    private static int attackEnergyCost(ItemStack stack) {
        return Constants.ELECTRIC_MENDING_ATTACK_ENERGY_COST * new StageHandler(stack).getStage().getEnergyCostMultiplier();
    }

    /**
     * Whether the Maw holds enough FE for a full-power hit. Below it, the hit is weakened
     * (see {@link #getHitDamageMultiplier}).
     */
    public boolean canPowerHit(ItemStack stack) {
        var energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);

        return energy != null && energy.getEnergyStored() >= attackEnergyCost(stack);
    }

    /**
     * Multiplier for the whole melee hit: 1 with enough FE, a heavy penalty without.
     * Undying Flesh makes the Maw unbreakable, so the battery only buys the shock there (see {@link #onAttack}) and an
     * empty one costs no damage.
     */
    public float getHitDamageMultiplier(MutationHandler handler, ItemStack stack) {
        if (!handler.has(Mutations.ELECTRIC_MENDING) || handler.has(Mutations.UNDYING_FLESH) || canPowerHit(stack)) return 1.0F;

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

    /**
     * Each melee hit that lands costs FE, more at higher stages. A battery below the cost is kept for mending and the magnet.
     * With Undying Flesh the Maw never needs mending, so a hit costs nothing itself; instead it arcs lightning to nearby
     * mobs and pays per mob reached (see {@link #chainLightning}), so a crowd drains more.
     */
    @Override
    public void onAttack(ItemStack stack, LivingEntity target, Level level) {
        if (level.isClientSide()) return;

        MutationHandler handler = new MutationHandler(stack);

        if (!handler.has(Mutations.ELECTRIC_MENDING)) return;

        var energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);

        if (energy == null) return;

        if (handler.has(Mutations.UNDYING_FLESH)) {
            int costPerArc = Constants.ELECTRIC_MENDING_SHOCK_ENERGY_COST_PER_ARC * new StageHandler(stack).getStage().getEnergyCostMultiplier();

            chainLightning(target, (ServerLevel) level, energy, costPerArc);
            return;
        }

        if (!canPowerHit(stack)) return;

        energy.extractEnergy(attackEnergyCost(stack), false);
    }

    /**
     * Arcs from the struck target to the nearest other mobs, paying {@code costPerArc} FE for each. The battery limits
     * how many mobs are reached, and nothing is spent when there is no one to arc to. Credited to the attacker, who is never hit.
     */
    private static void chainLightning(LivingEntity target, ServerLevel level, IEnergyStorage energy, int costPerArc) {
        // hurtEnemy runs right after the damage, so the target's last attacker is the wielder.
        LivingEntity attacker = target.getLastHurtByMob();

        DamageSource source = attacker instanceof Player player
            ? level.damageSources().playerAttack(player)
            : level.damageSources().magic();

        AABB area = target.getBoundingBox().inflate(Constants.ELECTRIC_MENDING_CHAIN_RANGE);

        List<LivingEntity> arcTargets = level.getEntitiesOfClass(LivingEntity.class, area, entity ->
            entity != target && entity != attacker && entity.isAlive() && !entity.isAlliedTo(target) && !(entity instanceof Player) && !(entity instanceof TamableAnimal tamed && tamed.isTame()));

        arcTargets.sort(Comparator.comparingDouble(target::distanceToSqr));

        Vec3 from = target.position().add(0.0D, target.getBbHeight() / 2.0D, 0.0D);

        int affordable = energy.getEnergyStored() / costPerArc;
        List<LivingEntity> victims = arcTargets.stream().limit(Math.min(Constants.ELECTRIC_MENDING_CHAIN_TARGETS, affordable)).toList();

        energy.extractEnergy(costPerArc * victims.size(), false);

        for (LivingEntity victim : victims) {
            victim.hurt(source, Constants.ELECTRIC_MENDING_CHAIN_DAMAGE);

            Vec3 to = victim.position().add(0.0D, victim.getBbHeight() / 2.0D, 0.0D);
            int steps = (int) (from.distanceTo(to) * 4.0D);

            for (int i = 0; i <= steps; i++) {
                Vec3 point = from.lerp(to, steps == 0 ? 0.0D : (double) i / steps);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }

        if (!victims.isEmpty()) {
            level.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4F, 1.6F);
        }
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
