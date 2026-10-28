package net.rywir.ravenousmaw.content.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.rywir.ravenousmaw.system.OxytocinergicEsca;

/**
 * Vanilla has no raid event, but it rewards every hero of a won raid with Hero of the Village at
 * amplifier = Bad Omen level - 1, so that effect is how a raid win is detected (Oxytocinergic Esca).
 * The same happens for the effect given by a command.
 */
public class RavenousMobEffectEvent {
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getEffectInstance().getEffect().is(MobEffects.HERO_OF_THE_VILLAGE)) {
            OxytocinergicEsca.onRaidWon(player, event.getEffectInstance().getAmplifier() + 1);
        }
    }
}
