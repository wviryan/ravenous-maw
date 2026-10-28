package net.rywir.ravenousmaw.datagen.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.loot.PiglinBarterModifier;

import java.util.concurrent.CompletableFuture;

public class RavenousGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public RavenousGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, RavenousMaw.MOD_ID);
    }

    @Override
    protected void start() {
        add("piglin_barter", new PiglinBarterModifier(new LootItemCondition[]{
            LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("gameplay/piglin_bartering")).build()
        }));
    }
}
