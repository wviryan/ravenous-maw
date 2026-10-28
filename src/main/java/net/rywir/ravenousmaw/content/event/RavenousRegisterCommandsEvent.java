package net.rywir.ravenousmaw.content.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.rywir.ravenousmaw.content.command.*;

public class RavenousRegisterCommandsEvent {
    @SubscribeEvent
    public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        RavenousCommands.register(event.getDispatcher());
    }
}
