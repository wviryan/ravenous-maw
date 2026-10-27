package net.rywir.ravenousmaw.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rywir.ravenousmaw.RavenousMaw;

public class RavenousCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RavenousMaw.MOD_ID);

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    static {
        CREATIVE_MODE_TABS.register("ravenous_maw_creative_mode_tab", () -> CreativeModeTab.builder().icon(() -> new ItemStack(RavenousItems.PIGLIN_PIE.get())).title(Component.translatable("creativetab.ravenousmaw.ravenous_tab")).displayItems((itemDisplayParameters, output) -> {
        output.accept(RavenousItems.RAVENOUS_MAW_LATENT);
        output.accept(RavenousItems.RAVENOUS_MAW_ADVANCED);
        output.accept(RavenousItems.RAVENOUS_MAW_NOBLE);
        output.accept(RavenousItems.RAVENOUS_MAW_EXCELSIOR);

        output.accept(RavenousItems.PIGLIN_PIE);
        output.accept(RavenousItems.CHORUS_CRACKER);
        output.accept(RavenousItems.SCULK_CROISSANT);

        output.accept(RavenousItems.CONTAMINATION_CORE);
        output.accept(RavenousItems.MUTATION_CODEX);

        output.accept(RavenousBlocks.MUTATION_MATRIX.get());
    }).build());
    }
}