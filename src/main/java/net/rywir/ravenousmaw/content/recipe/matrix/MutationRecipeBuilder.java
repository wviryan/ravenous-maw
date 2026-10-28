package net.rywir.ravenousmaw.content.recipe.matrix;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.registry.Mutations;
import org.jspecify.annotations.Nullable;

public class MutationRecipeBuilder implements RecipeBuilder {
    private final Ingredient base;
    private final Ingredient feast;
    private final Ingredient mutagen;

    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();

    private MutationRecipeBuilder(Ingredient base, Ingredient feast, Ingredient mutagen) {
        this.base = base;
        this.feast = feast;
        this.mutagen = mutagen;
    }

    public static MutationRecipeBuilder mutation(Ingredient base, Ingredient feast, Ingredient mutagen) {
        return new MutationRecipeBuilder(base, feast, mutagen);
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String s) {
        return this;
    }

    /** {@code <first base item>_<mutation>}, the id the provider used to pass by hand. */
    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        Item baseItem = this.base.items().findFirst().orElseThrow().value();
        Mutations target = Mutations.byMutagen(this.mutagen.items().findFirst().orElseThrow().value());
        if (target == null) {
            throw new IllegalStateException("Mutation recipe has a mutagen that belongs to no mutation");
        }

        String path = BuiltInRegistries.ITEM.getKey(baseItem).getPath() + "_" + target.name().toLowerCase();
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, path));
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
        // No category folder, so advancements stay at recipes/<id> as before.
        output.accept(id, new MutationRecipe(base, feast, mutagen), this.advancementBuilder.build(output, id, (String) null));
    }
}
