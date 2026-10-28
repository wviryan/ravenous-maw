package net.rywir.ravenousmaw.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.datagen.provider.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** In 26.1 the data run fires a client event (models and item definitions) and a server event (data pack content). */
@EventBusSubscriber(modid = RavenousMaw.MOD_ID)
public class DataGenerators {
    @SubscribeEvent
    public static void generateClient(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(true, new RavenousModelProvider(packOutput));
    }

    @SubscribeEvent
    public static void generateServer(GatherDataEvent.Server event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new RavenousBlockTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new RavenousItemTagsProvider(packOutput, lookupProvider));
        generator.addProvider(true, new RavenousRecipeProvider.Runner(packOutput, lookupProvider));
        generator.addProvider(true, new RavenousAdvancementProvider(packOutput, lookupProvider));
        generator.addProvider(true, new RavenousGlobalLootModifierProvider(packOutput, lookupProvider));
        generator.addProvider(true, new LootTableProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(RavenousBlockLootProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
    }
}
