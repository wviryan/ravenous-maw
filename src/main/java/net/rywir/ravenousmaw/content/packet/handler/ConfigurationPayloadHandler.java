package net.rywir.ravenousmaw.content.packet.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.rywir.ravenousmaw.content.gui.menu.ConfigurationMenu;
import net.rywir.ravenousmaw.content.packet.payload.ConfigurationPayload;
import net.rywir.ravenousmaw.content.packet.payload.ConfigurationSyncPayload;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.AssimilationHandler;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.util.dstruct.ConfigAction;

public class ConfigurationPayloadHandler {
    public static void handle(final ConfigurationPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) return;

                // Edit the stack the open menu is bound to, never whatever is in hand.
                if (!(player.containerMenu instanceof ConfigurationMenu menu)) return;

                ItemStack stack = menu.getStack();

                if (!stack.is(RavenousItemTagsProvider.MAW)) return;

                // Keys come from the client as free strings, so reject unknown ones.
                Mutations.Parameters parameter = Mutations.Parameters.byKey(payload.paramkey());

                if (parameter == null) return;

                // The Antenna switch belongs to the player, not to the Maw
                if (parameter == Mutations.Parameters.ELDRITCH_ANTENNA) {
                    configureAntenna(player, payload.action());
                    return;
                }

                MutationHandler handler = new MutationHandler(stack);

                StageHandler stageHandler = new StageHandler(stack);
                Stages stage = stageHandler.getStage();

                switch (payload.action()) {
                    case DECREMENT -> handler.prevConfigVal(parameter, player.level(), stage);
                    case INCREMENT -> handler.nextConfval(parameter, player.level(), stage);
                    case RESET -> handler.resetConfigVal(parameter, player.level());
                }

                int newValue = handler.getConfigVal(parameter);

                PacketDistributor.sendToPlayer(
                    player,
                    new ConfigurationSyncPayload(payload.paramkey(), newValue)
                );
            }
        );
    }

    private static void configureAntenna(ServerPlayer player, ConfigAction action) {
        // The client only lists the switch once earned, but the key is a free string, so check again
        if (!AssimilationHandler.has(player, Assimilations.ELDRITCH_ANTENNA)) return;

        Mutations.Parameters parameter = Mutations.Parameters.ELDRITCH_ANTENNA;
        int current = AssimilationHandler.getAntennaSwitch(player);

        int newValue = switch (action) {
            case DECREMENT -> parameter.prev(current);
            case INCREMENT -> parameter.next(current);
            case RESET -> parameter.head();
        };

        AssimilationHandler.setAntennaSwitch(player, newValue);

        PacketDistributor.sendToPlayer(player, new ConfigurationSyncPayload(parameter.key(), newValue));
    }
}
