package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.Enchantments;
import net.rywir.ravenousmaw.content.effect.CombustiveMobEffect;
import net.rywir.ravenousmaw.system.EnchantmentHandler;
import net.rywir.ravenousmaw.util.Constants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousMobEffects;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.interfaces.IMutationAbility;
import net.rywir.ravenousmaw.util.Configs;

public class CombustiveEnzyme implements IMutationAbility {
    private static final int DURATION = 80;

    @Override
    public void onAttack(ItemStack stack, LivingEntity target, Level level) {
        if (level.isClientSide()) return;

        MutationHandler handler = new MutationHandler(stack);

        if (!handler.has(Mutations.COMBUSTIVE_ENZYME)) {
            return;
        }

        // Fire Aspect: no two-hit combo, the hit lights a short fuse and the target explodes right after.
        boolean hasFireAspect = new EnchantmentHandler(stack).getEnchLevel(Enchantments.FIRE_ASPECT, level.registryAccess()) > 0;

        if (hasFireAspect) {
            lightFuse(target, (ServerLevel) level);
            return;
        }

        boolean hasEffect = target.hasEffect(RavenousMobEffects.COMBUSTIVE);

        int durationInSeconds = 5;

        if (!hasEffect) {
            target.addEffect(new MobEffectInstance(RavenousMobEffects.COMBUSTIVE, durationInSeconds * 20, 0));
        } else {
            target.removeEffect(RavenousMobEffects.COMBUSTIVE);
            ((ServerLevel) level).sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
        }
    }

    private static void lightFuse(LivingEntity target, ServerLevel level) {
        MobEffectInstance current = target.getEffect(RavenousMobEffects.COMBUSTIVE);

        // A running fuse keeps its timer. Anything else is replaced so a stale normal effect doesn't resurface after the blast.
        if (current != null && current.getAmplifier() == CombustiveMobEffect.FUSE_AMPLIFIER) {
            return;
        }

        target.removeEffect(RavenousMobEffects.COMBUSTIVE);
        target.addEffect(new MobEffectInstance(RavenousMobEffects.COMBUSTIVE, Constants.COMBUSTIVE_ENZYME_FUSE_TICKS, CombustiveMobEffect.FUSE_AMPLIFIER));

        level.playSound(null, target.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
    }

    @Override
    public float getAttackDamageBonus(MutationHandler handler, ItemStack stack, LivingEntity target, Level level, Stages stage) {
        boolean hasCombustiveEnzyme = handler.has(Mutations.COMBUSTIVE_ENZYME);

        if (!hasCombustiveEnzyme) {
            return 0.0F;
        }

        Holder<MobEffect> effect = RavenousMobEffects.COMBUSTIVE;
        boolean hasEffect = target.hasEffect(effect);

        // Fuse hits only get the explosion, not the stage bonus.
        if (!hasEffect || target.getEffect(effect).getAmplifier() == CombustiveMobEffect.FUSE_AMPLIFIER) {
            return 0.0F;
        }

        float bonus = switch (stage) {
            case Stages.ADVANCED -> ((float) Configs.COMBUSTIVE_ENZYME_ADVANCED_DAMAGE_BONUS.getAsDouble());
            case Stages.NOBLE -> ((float) Configs.COMBUSTIVE_ENZYME_NOBLE_DAMAGE_BONUS.getAsDouble());
            case Stages.EXCELSIOR -> ((float) Configs.COMBUSTIVE_ENZYME_EXCELSIOR_DAMAGE_BONUS.getAsDouble());
            default -> 0.0F;
        };

        return bonus;
    }

    @Override
    public void onInstability(MutationHandler mutationHandler, ItemStack stack, Player player) {
        if (player.level().isClientSide()) return;

        MutationHandler handler = new MutationHandler(stack);

        if (!handler.has(Mutations.COMBUSTIVE_ENZYME)) return;

        player.addEffect(new MobEffectInstance(RavenousMobEffects.COMBUSTIVE, DURATION));
        player.displayClientMessage(Component.translatable("instability_message.ravenousmaw.combustive_enzyme"), true);
    }
}
