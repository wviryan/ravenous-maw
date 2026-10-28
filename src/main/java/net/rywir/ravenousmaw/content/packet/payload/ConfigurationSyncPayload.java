package net.rywir.ravenousmaw.content.packet.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.rywir.ravenousmaw.RavenousMaw;
import org.jetbrains.annotations.NotNull;

public record ConfigurationSyncPayload(String key, int val) implements CustomPacketPayload {
    public static final Type<ConfigurationSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "configuration_sync_client"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurationSyncPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ConfigurationSyncPayload::key,
            ByteBufCodecs.INT,        ConfigurationSyncPayload::val,
            ConfigurationSyncPayload::new
        );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
