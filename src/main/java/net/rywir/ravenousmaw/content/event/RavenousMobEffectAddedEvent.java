package net.rywir.ravenousmaw.content.event;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousMobEffects;
import net.rywir.ravenousmaw.system.MutationHandler;

public class RavenousMobEffectAddedEvent {
    @SubscribeEvent
    public static void onMobEffectAddedEvent(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Drinking a potion occupies a hand, so the Maw can also be held in the offhand.
        MutationHandler handler = null;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);

            if (!held.is(RavenousItemTagsProvider.MAW)) {
                continue;
            }

            MutationHandler candidate = new MutationHandler(held);

            if (candidate.has(Mutations.SYMBIOTIC_AID)) {
                handler = candidate;
                break;
            }
        }

        if (handler == null) {
            return;
        }

        int isImmunityActive = handler.getConfigVal(Mutations.Parameters.SYMBIOTIC_IMMUNITY);

        if (isImmunityActive == 0) {
            return;
        }

        Holder<MobEffect> effect = event.getEffectInstance().getEffect();

        MobEffect instance = effect.value();

        boolean isInfection = instance == RavenousMobEffects.SYMBIOTIC_INFECTION.get();
        boolean isHarmful = instance.getCategory() == MobEffectCategory.HARMFUL;

        // Immunity only cleanses harmful effects, and never the Maw's own infection.
        if (isInfection || !isHarmful) {
            return;
        }

        // Deferred so the effect is removed after the add event has finished.
        player.server.execute(() -> player.removeEffect(effect));
    }
}