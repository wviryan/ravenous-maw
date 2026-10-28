package net.rywir.ravenousmaw.registry;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.effect.CombustiveMobEffect;
import net.rywir.ravenousmaw.content.effect.SymbioticInfectionMobEffect;

public class RavenousMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, RavenousMaw.MOD_ID);

    public static final DeferredHolder<MobEffect, CombustiveMobEffect> COMBUSTIVE = MOB_EFFECTS.register("combustive",
        () -> new CombustiveMobEffect(MobEffectCategory.HARMFUL, 8889187, ParticleTypes.LAVA));

    public static final DeferredHolder<MobEffect, SymbioticInfectionMobEffect> SYMBIOTIC_INFECTION = MOB_EFFECTS.register("symbiotic_infection",
        () -> new SymbioticInfectionMobEffect(MobEffectCategory.HARMFUL, 4000, PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F)));

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }
}
