package net.rywir.ravenousmaw.content.packet.handler;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.rywir.ravenousmaw.content.client.ClientHooks;
import net.rywir.ravenousmaw.content.packet.payload.AssimilationSyncPayload;

public class AssimilationSyncPayloadHandler {
    // Registered with playToClient, so this only runs on the client (see ConfigurationSyncPayloadHandler)
    public static void handle(final AssimilationSyncPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> ClientHooks.updateAssimilations(payload.keys()));
    }
}
