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
import net.rywir.ravenousmaw.registry.Mutations;
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

    /** True if the player earned the assimilation, whether or not a Maw is carried. */
    public static boolean has(Player player, Assimilations assimilation) {
        return player.getData(RavenousAttachments.ASSIMILATIONS).has(assimilation);
    }

    /** True if the player earned the assimilation and carries a Maw: the condition every effect checks. */
    public static boolean isActive(Player player, Assimilations assimilation) {
        return has(player, assimilation) && carriesMaw(player);
    }

    /** Eldritch Antenna is active when earned, a Maw is carried and the player has not switched it off. */
    public static boolean isAntennaActive(Player player) {
        return isActive(player, Assimilations.ELDRITCH_ANTENNA) && player.getData(RavenousAttachments.ANTENNA_ENABLED);
    }

    /** The Antenna switch as a config parameter value (1 on, 0 off). */
    public static int getAntennaSwitch(Player player) {
        return player.getData(RavenousAttachments.ANTENNA_ENABLED) ? 1 : 0;
    }

    public static void setAntennaSwitch(Player player, int value) {
        player.setData(RavenousAttachments.ANTENNA_ENABLED, value == 1);
    }

    /**
     * Eldritch Antenna unlock: one carried Maw holds every mutation, and the player earned every other
     * assimilation. Cheap enough to poll, and it ignores how the last piece was obtained.
     */
    public static void checkEldritchAntenna(ServerPlayer player) {
        AssimilationData data = player.getData(RavenousAttachments.ASSIMILATIONS);

        if (data.has(Assimilations.ELDRITCH_ANTENNA)) return;

        for (Assimilations assimilation : Assimilations.values()) {
            if (assimilation != Assimilations.ELDRITCH_ANTENNA && !data.has(assimilation)) return;
        }

        if (!carriesFullyMutatedMaw(player)) return;

        grant(player, Assimilations.ELDRITCH_ANTENNA);
    }

    private static boolean carriesFullyMutatedMaw(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (hasAllMutations(stack)) return true;
        }

        return hasAllMutations(player.getOffhandItem());
    }

    private static boolean hasAllMutations(ItemStack stack) {
        return stack.is(RavenousItemTagsProvider.MAW) && new MutationHandler(stack).matchMutations().size() == Mutations.values().length;
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
