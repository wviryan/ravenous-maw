package net.rywir.ravenousmaw.content.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.rywir.ravenousmaw.content.command.*;

public class RavenousRegisterCommandsEvent {
    @SubscribeEvent
    public static void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        MutateCommand.register(event.getDispatcher());
        UpCommand.register(event.getDispatcher());
        DismutateCommand.register(event.getDispatcher());
        ChargeCommand.register(event.getDispatcher());
        SurchargeCommand.register(event.getDispatcher());
        DestabilizeCommand.register(event.getDispatcher());
        TickCommand.register(event.getDispatcher());
        DamageCommand.register(event.getDispatcher());
        AssimilateCommand.register(event.getDispatcher());
    }
}
