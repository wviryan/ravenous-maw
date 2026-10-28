package net.rywir.ravenousmaw.content.event;

import net.neoforged.neoforge.event.entity.living.EnderManAngerEvent;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.system.AssimilationHandler;

/**
 * Nictitating Membrane: an Enderman does not take offence at being looked in the eye. This only blocks
 * the gaze check; an Enderman the player hurt still fights back (see RavenousLivingChangeTargetEvent).
 */
public class RavenousEnderManAngerEvent {
    public static void onEnderManAngerEvent(EnderManAngerEvent event) {
        if (AssimilationHandler.isActive(event.getPlayer(), Assimilations.NICTITATING_MEMBRANE)) {
            event.setCanceled(true);
        }
    }
}
