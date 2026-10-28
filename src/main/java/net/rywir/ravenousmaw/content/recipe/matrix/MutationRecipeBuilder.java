package net.rywir.ravenousmaw.content.recipe.matrix;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class MutationRecipeBuilder implements RecipeBuilder {
    private final Ingredient base;
    private final Ingredient feast;
    private final Ingredient mutagen;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

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
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String s) {
        return this;
    }

    @Override
    public Item getResult() {
        ItemStack baseStack = this.base.getItems().length > 0 ? this.base.getItems()[0].copy() : ItemStack.EMPTY;
        if (baseStack.isEmpty()) return ItemStack.EMPTY.getItem();

        Mutations target = Mutations.byMutagen(mutagen.getItems()[0].getItem());
        if (target == null) return baseStack.getItem();

        MutationHandler handler = new MutationHandler(baseStack);
        handler.add(target);

        return handler.getStack().getItem();
    }

    @Override
    public void save(RecipeOutput output, Identifier id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("Mutation Recipe " + id + " has no advancement criteria defined!");
        }

        Advancement.Builder advancement = output.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancement::addCriterion);

        MutationRecipe recipe = new MutationRecipe(base, feast, mutagen);
        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }
}
