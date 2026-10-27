package net.rywir.ravenousmaw.datagen.provider;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.registry.RavenousItems;

public class RavenousItemModelProvider extends ItemModelProvider {
    public RavenousItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, RavenousMaw.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(RavenousItems.PIGLIN_PIE.get());
        basicItem(RavenousItems.SCULK_CROISSANT.get());
        basicItem(RavenousItems.CHORUS_CRACKER.get());

        basicItem(RavenousItems.CONTAMINATION_CORE.get());
        basicItem(RavenousItems.MUTATION_CODEX.get());

        handheldItem(RavenousItems.RAVENOUS_MAW_LATENT);
        handheldItem(RavenousItems.RAVENOUS_MAW_ADVANCED);
        handheldItem(RavenousItems.RAVENOUS_MAW_NOBLE);
        handheldItem(RavenousItems.RAVENOUS_MAW_EXCELSIOR);
    }

    private void handheldItem(DeferredHolder<Item, ?> itemHolder) {
        String name = itemHolder.getId().getPath();
        withExistingParent(name, ResourceLocation.parse("item/handheld")).texture("layer0", ResourceLocation.fromNamespaceAndPath(RavenousMaw.MOD_ID,"item/" + name));
    }
}
