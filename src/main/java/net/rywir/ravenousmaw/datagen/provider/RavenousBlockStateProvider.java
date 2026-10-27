package net.rywir.ravenousmaw.datagen.provider;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.registry.RavenousBlocks;

public class RavenousBlockStateProvider extends BlockStateProvider {
    public RavenousBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, RavenousMaw.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        topSideBottom(RavenousBlocks.MUTATION_MATRIX);
    }

    private void topSideBottom(DeferredBlock<?> deferred) {
        String name = deferred.getId().getPath();
        Block block = deferred.get();

        ModelFile model = models().cubeBottomTop(name, modLoc("block/" + name + "_side"), modLoc("block/" + name + "_bottom"), modLoc("block/" + name + "_top"));

        simpleBlockWithItem(block, model);
    }
}
