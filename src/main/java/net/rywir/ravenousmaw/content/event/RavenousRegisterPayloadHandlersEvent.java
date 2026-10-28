package net.rywir.ravenousmaw.content.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.rywir.ravenousmaw.content.packet.handler.AssimilationSyncPayloadHandler;
import net.rywir.ravenousmaw.content.packet.handler.ConfigurationPayloadHandler;
import net.rywir.ravenousmaw.content.packet.handler.ConfigurationSyncPayloadHandler;
import net.rywir.ravenousmaw.content.packet.payload.AssimilationSyncPayload;
import net.rywir.ravenousmaw.content.packet.payload.ConfigurationPayload;
import net.rywir.ravenousmaw.content.packet.payload.ConfigurationSyncPayload;

public class RavenousRegisterPayloadHandlersEvent {
    @SubscribeEvent
    public static void onRegisterPayloadHandlersEvent(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
            ConfigurationPayload.TYPE,
            ConfigurationPayload.STREAM_CODEC,
            ConfigurationPayloadHandler::handle
        );

        registrar.playToClient(
            ConfigurationSyncPayload.TYPE,
            ConfigurationSyncPayload.STREAM_CODEC,
            ConfigurationSyncPayloadHandler::handle
        );

        registrar.playToClient(
            AssimilationSyncPayload.TYPE,
            AssimilationSyncPayload.STREAM_CODEC,
            AssimilationSyncPayloadHandler::handle
        );
    }
}
