package net.rywir.ravenousmaw.datagen.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.registry.RavenousBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class RavenousBlockTagsProvider extends BlockTagsProvider {
    public static final TagKey<Block> CORRECT_FOR_MAW = create("correct_for_maw");
    public static final TagKey<Block> INCORRECT_MAW = create("incorrect_for_maw");

    public RavenousBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, RavenousMaw.MOD_ID, existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(CORRECT_FOR_MAW)
            .addTags(BlockTags.SWORD_EFFICIENT)
            .addTags(BlockTags.MINEABLE_WITH_PICKAXE)
            .addTags(BlockTags.MINEABLE_WITH_AXE)
            .addTags(BlockTags.MINEABLE_WITH_SHOVEL)
            .addTags(BlockTags.MINEABLE_WITH_HOE);

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(RavenousBlocks.MUTATION_MATRIX.get());

        this.tag(INCORRECT_MAW);
    }

    private static @NotNull TagKey<Block> create(String name) {
        return BlockTags.create(Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, name));
    }

    private static @NotNull TagKey<Block> forge(String name) {
        return BlockTags.create(Identifier.fromNamespaceAndPath("c", name));
    }
}