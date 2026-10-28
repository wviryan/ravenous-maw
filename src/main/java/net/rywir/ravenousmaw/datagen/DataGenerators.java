package net.rywir.ravenousmaw.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.datagen.provider.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = RavenousMaw.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        BlockTagsProvider blockTagsProvider = new RavenousBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new RavenousItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeClient(), new RavenousItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new RavenousRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new RavenousAdvancementProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new RavenousBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new RavenousGlobalLootModifierProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(RavenousBlockLootProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
    }
}
