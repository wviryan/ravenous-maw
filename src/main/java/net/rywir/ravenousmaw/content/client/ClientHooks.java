package net.rywir.ravenousmaw.content.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.rywir.ravenousmaw.content.gui.screen.ConfigurationScreen;

/**
 * Entry points into client-only code for classes shared with the dedicated server.
 * Common classes (items, packet handlers) must not import {@code net.minecraft.client} themselves.
 * They call this class instead, and only from code paths that run on the client.
 * Nothing here is loaded on a dedicated server unless one of those paths is run by mistake.
 */
public final class ClientHooks {
    private ClientHooks() {}

    public static boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    /** Forwards a server-confirmed configuration value to the open configuration screen, if any. */
    public static void updateConfigurationScreen(String key, int val) {
        if (Minecraft.getInstance().screen instanceof ConfigurationScreen screen) {
            screen.update(key, val);
        }
    }
}
