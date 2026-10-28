package net.rywir.ravenousmaw.content.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.rywir.ravenousmaw.content.attachment.DimensionDeedTimer;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.RavenousAttachments;
import net.rywir.ravenousmaw.system.AssimilationHandler;
import net.rywir.ravenousmaw.util.Constants;

import java.util.Map;

/**
 * Grants the dimension assimilations after the player has stayed in the dimension for a random delay,
 * so merely stepping through a portal is not enough. Leaving the dimension cancels the countdown.
 * It is driven by the player's current dimension rather than by the travel event, so logging in
 * inside the dimension also counts and no timer has to be saved.
 */
public class RavenousDimensionDeedEvent {
    private static final Map<ResourceKey<Level>, Assimilations> DEED_BY_DIMENSION = Map.of(
        Level.NETHER, Assimilations.GILDED_PHEROMONES
    );

    @SubscribeEvent
    public static void onPlayerTickEvent(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DimensionDeedTimer timer = player.getData(RavenousAttachments.DIMENSION_DEED_TIMER);
        Assimilations deed = DEED_BY_DIMENSION.get(player.level().dimension());

        if (deed == null || player.getData(RavenousAttachments.ASSIMILATIONS).has(deed)) {
            timer.cancel();
            return;
        }

        if (!timer.isRunning()) {
            int range = Constants.DIMENSION_DEED_MAX_DELAY_TICKS - Constants.DIMENSION_DEED_MIN_DELAY_TICKS;
            timer.start(Constants.DIMENSION_DEED_MIN_DELAY_TICKS + player.getRandom().nextInt(range + 1));
        }

        if (timer.tick()) {
            AssimilationHandler.grant(player, deed);
        }
    }
}
