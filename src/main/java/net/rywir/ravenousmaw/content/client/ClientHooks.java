package net.rywir.ravenousmaw.content.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.rywir.ravenousmaw.content.attachment.AssimilationData;
import net.rywir.ravenousmaw.content.gui.menu.ConfigurationMenu;
import net.rywir.ravenousmaw.content.gui.screen.ConfigurationScreen;
import net.rywir.ravenousmaw.registry.RavenousAttachments;

import java.util.List;

/**
 * Entry points into client-only code for classes shared with the dedicated server.
 * Common classes (items, packet handlers) must not import {@code net.minecraft.client} themselves.
 * They call this class instead, and only from code paths that run on the client.
 * Nothing here is loaded on a dedicated server unless one of those paths is run by mistake.
 */
public final class ClientHooks {
    private ClientHooks() {}

    public static boolean isShiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }

    /** Forwards a server-confirmed configuration value to the open configuration screen, if any. */
    public static void updateConfigurationScreen(String key, int val) {
        // The stacks are kept on the local player so the screen can rebuild its pages without losing them.
        if (key.equals(ConfigurationMenu.STAT_OXYTOCIN_STACKS) && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setData(RavenousAttachments.OXYTOCIN_STACKS, val);
        }

        if (Minecraft.getInstance().screen instanceof ConfigurationScreen screen) {
            screen.update(key, val);
        }
    }

    /** Stores the server-sent assimilations on the local player (see RavenousAssimilationSyncEvent). */
    public static void updateAssimilations(List<String> keys) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.setData(RavenousAttachments.ASSIMILATIONS, AssimilationData.fromKeys(keys));
        }
    }

    /** @return how many assimilations the local player has earned, 0 when there is no player (e.g. the main menu) */
    public static int getAssimilationCount() {
        var player = Minecraft.getInstance().player;
        return player == null ? 0 : player.getData(RavenousAttachments.ASSIMILATIONS).count();
    }
}
