package net.rywir.ravenousmaw.content.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.registry.RavenousMobEffects;
import org.jetbrains.annotations.NotNull;

public class CombustiveMobEffect extends MobEffect {
    public CombustiveMobEffect(MobEffectCategory category, int color, ParticleOptions particle) {
        super(category, color, particle);
    }

    private static final float EXPLOSION_POWER = 1.5F;

    @Override
    public void onMobHurt(LivingEntity target, int amplifier, @NotNull DamageSource damageSource, float amount) {
        Level level = target.level();

        if (level.isClientSide()) {
            return;
        }

        if (!damageSource.is(DamageTypeTags.IS_FIRE)) {
            return;
        }

        // Credited to whoever dealt the fire damage, who is also spared from the blast.
        Entity owner = damageSource.getEntity();

        level.explode(
            owner,
            null,
            new ExplosionDamageCalculator() {
                @Override
                public boolean shouldDamageEntity(@NotNull Explosion explosion, @NotNull Entity entity) {
                    return entity != owner;
                }
            },
            target.getX(),
            target.getY() + 0.5,
            target.getZ(),
            EXPLOSION_POWER,
            false,
            Level.ExplosionInteraction.NONE
        );

        // Vanilla calls this while iterating the live effect map, so removing the effect here would throw a
        // ConcurrentModificationException when the target has other effects. Remove it after the loop instead.
        target.getServer().execute(() -> target.removeEffect(RavenousMobEffects.COMBUSTIVE));
    }
}
