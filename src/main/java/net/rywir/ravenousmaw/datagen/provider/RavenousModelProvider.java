package net.rywir.ravenousmaw.datagen.provider;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.registry.RavenousBlocks;
import net.rywir.ravenousmaw.registry.RavenousItems;

/**
 * Block states, block models, item models and the client item definitions (26.1 reads items from
 * {@code assets/<mod>/items}). The Mutation Matrix's block item definition is derived from its block model.
 */
public class RavenousModelProvider extends ModelProvider {
    public RavenousModelProvider(PackOutput output) {
        super(output, RavenousMaw.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Textures <name>_side, <name>_bottom and <name>_top.
        blockModels.createTrivialBlock(RavenousBlocks.MUTATION_MATRIX.get(), TexturedModel.CUBE_TOP_BOTTOM);

        itemModels.generateFlatItem(RavenousItems.PIGLIN_PIE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RavenousItems.SCULK_CROISSANT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RavenousItems.CHORUS_CRACKER.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(RavenousItems.CONTAMINATION_CORE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(RavenousItems.MUTATION_CODEX.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(RavenousItems.RAVENOUS_MAW_LATENT.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(RavenousItems.RAVENOUS_MAW_ADVANCED.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(RavenousItems.RAVENOUS_MAW_NOBLE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
    }
}
