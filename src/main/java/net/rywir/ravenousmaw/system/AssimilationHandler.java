package net.rywir.ravenousmaw.system;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.content.attachment.AssimilationData;
import net.rywir.ravenousmaw.content.event.RavenousAssimilationSyncEvent;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.RavenousAttachments;

/**
 * Single entry point for assimilations on a player. Earning one goes through {@link #grant} so the
 * client sync, the feedback (and later the advancement) are never forgotten.
 */
public class AssimilationHandler {
    /** True if any inventory slot (hotbar, backpack, offhand) holds a Maw. */
    public static boolean carriesMaw(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(RavenousItemTagsProvider.MAW)) return true;
        }

        return player.getOffhandItem().is(RavenousItemTagsProvider.MAW);
    }

    /** True if the player earned the assimilation and carries a Maw: the condition every effect checks. */
    public static boolean isActive(Player player, Assimilations assimilation) {
        return player.getData(RavenousAttachments.ASSIMILATIONS).has(assimilation) && carriesMaw(player);
    }

    /** Records the deed whatever the Maw's stage, even with no Maw on hand. Does nothing if already earned. */
    public static void grant(ServerPlayer player, Assimilations assimilation) {
        AssimilationData data = player.getData(RavenousAttachments.ASSIMILATIONS);

        if (!data.grant(assimilation)) return;

        RavenousAssimilationSyncEvent.sync(player);

        player.displayClientMessage(Component.translatable("ravenousmaw.assimilation_granted", Component.translatable("assimilation.ravenousmaw." + assimilation.getKey())), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 1.0F, 0.8F);
    }
}
