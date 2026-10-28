package net.rywir.ravenousmaw.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.compat.category.MutationMatrixRecipeCategory;
import net.rywir.ravenousmaw.content.gui.screen.MutationMatrixScreen;
import net.rywir.ravenousmaw.content.recipe.feeding.FeedingRecipe;
import net.rywir.ravenousmaw.content.recipe.matrix.MutationRecipe;
import net.rywir.ravenousmaw.registry.RavenousItems;
import net.rywir.ravenousmaw.registry.RavenousRecipes;
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

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        List<MutationRecipe> mutationRecipes = manager.getAllRecipesFor(RavenousRecipes.MUTATION_RECIPE_TYPE.get()).stream().map(RecipeHolder::value).toList();
        registration.addRecipes(MutationMatrixRecipeCategory.MUTATION_RECIPE_RECIPE_TYPE, mutationRecipes);

        List<RecipeHolder<CraftingRecipe>> feedingRecipes = new ArrayList<>();

        for (var maw : RavenousItems.MAWS.getEntries()) {
            Stages stage = HelperData.getStage(maw);

            ItemStack input = maw.get().getDefaultInstance();
            int max = input.getMaxDamage();
            input.setDamageValue(max - 1);

            for (var entry : FeedingRecipe.REPAIR_MAP.get(stage).keySet()) {
                ItemStack feast = new ItemStack(entry);

                ItemStack output = input.copy();
                int amount = FeedingRecipe.REPAIR_MAP.get(stage).get(entry);
                output.setDamageValue(max - (1 + amount));

                ShapelessRecipe dummyRecipe = new ShapelessRecipe(
                    "ravenous",
                    CraftingBookCategory.MISC,
                    output,
                    NonNullList.of(Ingredient.EMPTY,
                        Ingredient.of(input),
                        Ingredient.of(feast)
                    )
                );

                Identifier id = Identifier.fromNamespaceAndPath("ravenous", "jei_feeding_" + stage.name().toLowerCase() + "_" + BuiltInRegistries.ITEM.getKey(entry.asItem()).getPath());
                RecipeHolder<CraftingRecipe> holder = new RecipeHolder<>(id, dummyRecipe);
                feedingRecipes.add(holder);
            }
        }

        registration.addRecipes(RecipeTypes.CRAFTING, feedingRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(MutationMatrixScreen.class, 89, 35, 22, 20, MutationMatrixRecipeCategory.MUTATION_RECIPE_RECIPE_TYPE);
    }
}
