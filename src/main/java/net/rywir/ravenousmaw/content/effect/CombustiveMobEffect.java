package net.rywir.ravenousmaw.content.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
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

    /** Amplifier of the short-fuse variant applied by Fire Aspect Maws; it explodes when it runs out. */
    public static final int FUSE_AMPLIFIER = 1;

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return amplifier == FUSE_AMPLIFIER && duration == 1;
    }

    @Override
    public boolean applyEffectTick(@NotNull ServerLevel level, @NotNull LivingEntity target, int amplifier) {
        if (amplifier == FUSE_AMPLIFIER) {
            // The effect carries no source, so the last attacker is credited and spared from the blast.
            explode(target, target.getLastHurtByMob());
        }

        return true;
    }

    @Override
    public void onMobHurt(@NotNull ServerLevel level, @NotNull LivingEntity target, int amplifier, @NotNull DamageSource damageSource, float amount) {
        // The fuse variant explodes on its own timer, not on fire damage.
        if (amplifier == FUSE_AMPLIFIER || !damageSource.is(DamageTypeTags.IS_FIRE)) {
            return;
        }

        // Credited to whoever dealt the fire damage, who is also spared from the blast.
        explode(target, damageSource.getEntity());

        // Vanilla calls this while iterating the live effect map, so removing the effect here would throw a
        // ConcurrentModificationException when the target has other effects. Remove it after the loop instead.
        level.getServer().execute(() -> target.removeEffect(RavenousMobEffects.COMBUSTIVE));
    }

    private static void explode(LivingEntity target, Entity owner) {
        target.level().explode(
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
    }
}
