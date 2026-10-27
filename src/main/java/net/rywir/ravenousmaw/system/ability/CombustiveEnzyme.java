package net.rywir.ravenousmaw.system.ability;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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

        boolean hasEffect = target.hasEffect(RavenousMobEffects.COMBUSTIVE);

        int durationInSeconds = 5;

        if (!hasEffect) {
            target.addEffect(new MobEffectInstance(RavenousMobEffects.COMBUSTIVE, durationInSeconds * 20, 0));
        } else {
            target.removeEffect(RavenousMobEffects.COMBUSTIVE);
            ((ServerLevel) level).sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
        }
    }

    @Override
    public float getAttackDamageBonus(MutationHandler handler, ItemStack stack, LivingEntity target, Level level, Stages stage) {
        boolean hasCombustiveEnzyme = handler.has(Mutations.COMBUSTIVE_ENZYME);

        if (!hasCombustiveEnzyme) {
            return 0.0F;
        }

        Holder<MobEffect> effect = RavenousMobEffects.COMBUSTIVE;
        boolean hasEffect = target.hasEffect(effect);

        if (!hasEffect) {
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
