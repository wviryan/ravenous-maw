package net.rywir.ravenousmaw.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.*;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.compat.category.MutationMatrixRecipeCategory;
import net.rywir.ravenousmaw.content.gui.screen.MutationMatrixScreen;
import net.rywir.ravenousmaw.content.recipe.feeding.FeedingRecipe;
import net.rywir.ravenousmaw.content.recipe.matrix.MutationRecipe;
import net.rywir.ravenousmaw.registry.RavenousItems;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.util.HelperData;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class RavenousJEIPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
         return Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new MutationMatrixRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    /**
     * Recipes are no longer synced to clients in 26.1 (only their displays are), so both recipe lists are
     * rebuilt here from the same rules the data generator uses.
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(MutationMatrixRecipeCategory.MUTATION_RECIPE_RECIPE_TYPE, createMutationRecipes());
        registration.addRecipes(RecipeTypes.CRAFTING, createFeedingRecipes());
    }

    /** One display recipe per Maw and per mutation of its stage or lower (same rule as {@code RavenousRecipeProvider}). */
    private static List<MutationRecipe> createMutationRecipes() {
        List<MutationRecipe> recipes = new ArrayList<>();

        for (Mutations mutation : Mutations.values()) {
            for (var maw : RavenousItems.MAWS.getEntries()) {
                if (mutation.stage().getId() > HelperData.getStage(maw).getId()) {
                    continue;
                }

                recipes.add(new MutationRecipe(
                    Ingredient.of(maw.get()),
                    Ingredient.of(mutation.stage().getFeast()),
                    Ingredient.of(mutation.mutagen())
                ));
            }
        }

        return recipes;
    }

    /** Display versions of the repair recipe: a nearly broken Maw plus a Feast gives the repaired Maw. */
    private static List<RecipeHolder<CraftingRecipe>> createFeedingRecipes() {
        List<RecipeHolder<CraftingRecipe>> feedingRecipes = new ArrayList<>();

        for (var maw : RavenousItems.MAWS.getEntries()) {
            Stages stage = HelperData.getStage(maw);
            int max = maw.get().getDefaultInstance().getMaxDamage();

            for (var entry : FeedingRecipe.REPAIR_MAP.get(stage).entrySet()) {
                // Ingredients match items only, so the damage shows on the result alone.
                DataComponentPatch repaired = DataComponentPatch.builder()
                    .set(DataComponents.DAMAGE, max - (1 + entry.getValue()))
                    .build();

                ShapelessRecipe dummyRecipe = new ShapelessRecipe(
                    new Recipe.CommonInfo(false),
                    new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "ravenous"),
                    new ItemStackTemplate(maw.get(), repaired),
                    List.of(Ingredient.of(maw.get()), Ingredient.of(entry.getKey()))
                );

                Identifier id = Identifier.fromNamespaceAndPath("ravenous", "jei_feeding_" + stage.name().toLowerCase() + "_" + BuiltInRegistries.ITEM.getKey(entry.getKey().asItem()).getPath());
                feedingRecipes.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), dummyRecipe));
            }
        }

        return feedingRecipes;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(MutationMatrixScreen.class, 89, 35, 22, 20, MutationMatrixRecipeCategory.MUTATION_RECIPE_RECIPE_TYPE);
    }
}
