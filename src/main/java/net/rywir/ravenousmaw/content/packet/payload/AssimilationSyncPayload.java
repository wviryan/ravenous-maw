package net.rywir.ravenousmaw.content.packet.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.rywir.ravenousmaw.RavenousMaw;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Server -> client: the keys of every assimilation the receiving player has earned. */
public record AssimilationSyncPayload(List<String> keys) implements CustomPacketPayload {
    public static final Type<AssimilationSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "assimilation_sync_client"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AssimilationSyncPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), AssimilationSyncPayload::keys,
            AssimilationSyncPayload::new
        );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
