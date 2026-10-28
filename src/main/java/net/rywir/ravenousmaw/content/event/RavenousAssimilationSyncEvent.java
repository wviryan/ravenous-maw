package net.rywir.ravenousmaw.content.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.rywir.ravenousmaw.content.attachment.AssimilationData;
import net.rywir.ravenousmaw.content.packet.payload.AssimilationSyncPayload;
import net.rywir.ravenousmaw.registry.RavenousAttachments;

/**
 * Attachments are not synced to the client by NeoForge 21.1, so the client (tooltip, Codex hints)
 * learns about assimilations through {@link AssimilationSyncPayload}. The client player object is
 * rebuilt on login, respawn and dimension change, so each of those sends the data again.
 * Anything that grants an assimilation must call {@link #sync} afterwards.
 */
public class RavenousAssimilationSyncEvent {
    public static void sync(ServerPlayer player) {
        AssimilationData data = player.getData(RavenousAttachments.ASSIMILATIONS);
        PacketDistributor.sendToPlayer(player, new AssimilationSyncPayload(data.toKeys()));
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
}
