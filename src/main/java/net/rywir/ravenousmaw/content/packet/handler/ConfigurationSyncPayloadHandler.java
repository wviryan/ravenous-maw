package net.rywir.ravenousmaw.content.packet.handler;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.rywir.ravenousmaw.content.client.ClientHooks;
import net.rywir.ravenousmaw.content.packet.payload.ConfigurationSyncPayload;

public class ConfigurationSyncPayloadHandler {
    // Registered with playToClient, so this only runs on the client. The client classes stay behind ClientHooks
    // so this common class has no client imports for the dedicated server to trip over.
    public static void handle(final ConfigurationSyncPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> ClientHooks.updateConfigurationScreen(payload.key(), payload.val()));
    }
}
