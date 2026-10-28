package net.rywir.ravenousmaw.content.packet.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.util.dstruct.ConfigAction;
import org.jetbrains.annotations.NotNull;

public record ConfigurationPayload(String paramkey, ConfigAction action) implements CustomPacketPayload {
    public static final Type<ConfigurationPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "configuration_change"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurationPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ConfigurationPayload::paramkey,
            ByteBufCodecs.idMapper(
                i -> ConfigAction.values()[i],
                ConfigAction::ordinal
            ),
            ConfigurationPayload::action,
            ConfigurationPayload::new
        );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
