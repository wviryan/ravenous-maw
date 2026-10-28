package net.rywir.ravenousmaw.content.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousItems;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.RavenousEnergyStorage;

public class RavenousRegisterCapabilitiesEvent {
    @SubscribeEvent
    public static void onRegisterCapabilitiesEvent(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Energy.ITEM, (stack, itemAccess) -> {
                MutationHandler handler = new MutationHandler(stack);

                boolean hasElectricMending = handler.has(Mutations.ELECTRIC_MENDING);

                if (!hasElectricMending) {
                    return null;
                }

                return new RavenousEnergyStorage(itemAccess);
            },

            RavenousItems.RAVENOUS_MAW_LATENT.get(),
            RavenousItems.RAVENOUS_MAW_ADVANCED.get(),
            RavenousItems.RAVENOUS_MAW_NOBLE.get(),
            RavenousItems.RAVENOUS_MAW_EXCELSIOR.get()
        );
    }
}
