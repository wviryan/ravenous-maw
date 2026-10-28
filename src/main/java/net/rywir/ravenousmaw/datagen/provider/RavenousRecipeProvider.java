package net.rywir.ravenousmaw.datagen.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.rywir.ravenousmaw.content.item.MawItem;
import net.rywir.ravenousmaw.content.recipe.feeding.FeedingRecipe;
import net.rywir.ravenousmaw.content.recipe.matrix.MutationRecipeBuilder;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousBlocks;
import net.rywir.ravenousmaw.registry.RavenousItems;
import net.rywir.ravenousmaw.util.HelperData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class RavenousRecipeProvider extends RecipeProvider {
    private RavenousRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    /** The data generator entry point: 26.1 runs recipe providers through a runner that hands over the recipe output. */
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider registries, @NotNull RecipeOutput output) {
            return new RavenousRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName() {
            return "Ravenous Maw Recipes";
        }
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.MISC, RavenousItems.PIGLIN_PIE.get())
            .pattern(" A ")
            .pattern("BCB")
            .pattern(" A ")
            .define('A', Items.BLAZE_POWDER)
            .define('B', Items.MAGMA_CREAM)
            .define('C', Items.NETHER_WART)
            .unlockedBy("has_nether_wart", has(Items.NETHER_WART)).save(output);

        shaped(RecipeCategory.MISC, RavenousItems.CHORUS_CRACKER.get())
            .pattern(" A ")
            .pattern("BCB")
            .pattern(" A ")
            .define('A', Items.SHULKER_SHELL)
            .define('B', Items.ENDER_PEARL)
            .define('C', Items.CHORUS_FRUIT)
            .unlockedBy("has_chorus_fruit", has(Items.CHORUS_FRUIT)).save(output);

        shaped(RecipeCategory.MISC, RavenousItems.SCULK_CROISSANT.get())
            .pattern(" A ")
            .pattern("BCB")
            .pattern(" A ")
            .define('A', Items.ECHO_SHARD)
            .define('B', Items.AMETHYST_SHARD)
            .define('C', Items.GLOW_BERRIES)
            .unlockedBy("has_echo_shard", has(Items.ECHO_SHARD)).save(output);

        shaped(RecipeCategory.MISC, RavenousBlocks.MUTATION_MATRIX.get())
            .pattern("FEF")
            .pattern("DAD")
            .pattern("BCB")
            .define('A', RavenousItems.CONTAMINATION_CORE)
            .define('F', Items.BONE)
            .define('E', Items.LEATHER)
            .define('B', Items.CLAY_BALL)
            .define('D', Items.ROTTEN_FLESH)
            .define('C', Items.CAMPFIRE)
            .unlockedBy("has_contamination_core", has(RavenousItems.CONTAMINATION_CORE)).save(output);

        shaped(RecipeCategory.MISC, RavenousItems.RAVENOUS_MAW_LATENT.get())
            .pattern("CSC")
            .pattern(" B ")
            .pattern(" T ")
            .define('C', RavenousItems.CONTAMINATION_CORE)
            .define('S', Items.STRING)
            .define('B', Items.BONE)
            .define('T', Items.STICK)
            .unlockedBy("has_contamination_core", has(RavenousItems.CONTAMINATION_CORE)).save(output);

        shaped(RecipeCategory.MISC, RavenousItems.CONTAMINATION_CORE.get())
            .pattern("RSR")
            .pattern("SDS")
            .pattern("RSR")
            .define('R', Items.ROTTEN_FLESH)
            .define('D', Items.DIAMOND)
            .define('S', Items.SLIME_BALL)
            .unlockedBy("has_diamond", has(Items.DIAMOND)).save(output);

        shapeless(RecipeCategory.MISC, RavenousItems.MUTATION_CODEX.get())
            .requires(Items.BOOK)
            .requires(Items.ROTTEN_FLESH, 4)
            .unlockedBy("has_rotten_flesh", has(Items.ROTTEN_FLESH)).save(output);

        SpecialRecipeBuilder.special(FeedingRecipe::new).save(output, "ravenousmaw:feeding_recipe");

        for (Mutations mutation : Mutations.values()) {
            for (DeferredHolder<Item, ? extends Item> mawHolder : RavenousItems.MAWS.getEntries()) {
                if (!(mawHolder.get() instanceof MawItem)) {
                    continue;
                }

                if (!(mutation.stage().getId() <= HelperData.getStage(mawHolder).getId())) {
                    continue;
                }

                Item mutagenItem = mutation.mutagen();
                String unlockCriterionName = "has_" + mutagenItem.getDefaultInstance().getItem().toString().toLowerCase();

                MutationRecipeBuilder.mutation(
                    Ingredient.of(mawHolder.get()),
                    Ingredient.of(mutation.stage().getFeast()),
                    Ingredient.of(mutation.mutagen())
                ).unlockedBy(unlockCriterionName, has(mutagenItem)).save(output);
            }
        }
    }
}
