package net.rywir.ravenousmaw.content.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.system.AssimilationHandler;

/**
 * One hook for every assimilation that makes mobs ignore the player. Each assimilation adds its own
 * filter in {@link #ignores}. It also fires for brain-driven mobs (piglins, hoglins).
 */
public class RavenousLivingChangeTargetEvent {
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player)) return;

        LivingEntity mob = event.getEntity();

        // A mob the player hurt keeps its right to answer
        if (mob.getLastHurtByMob() == player) return;

        if (ignores(mob, player)) {
            event.setCanceled(true);
        }
    }

    private static boolean ignores(LivingEntity mob, Player player) {
        if (mob instanceof Piglin || mob instanceof PiglinBrute || mob instanceof Hoglin) {
            return AssimilationHandler.isActive(player, Assimilations.GILDED_PHEROMONES);
        }

        return false;
    }
}
