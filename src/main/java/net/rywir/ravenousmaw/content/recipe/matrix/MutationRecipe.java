package net.rywir.ravenousmaw.content.recipe.matrix;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.content.item.MawItem;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousRecipes;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;

public class MutationRecipe implements Recipe<MutationRecipeInput> {
    private final Ingredient base;
    private final Ingredient feast;
    private final Ingredient mutagen;

    public MutationRecipe(Ingredient base, Ingredient feast, Ingredient mutagen) {
        this.base = base;
        this.feast = feast;
        this.mutagen = mutagen;
    }

    @Override
    public boolean matches(MutationRecipeInput input, Level level) {
        if (level.isClientSide()) return false;

        if (!(this.base.test(input.base()) && this.feast.test(input.feast()) && this.mutagen.test(input.mutagen()))) {
            return false;
        }

        Mutations target = Mutations.byMutagen(input.mutagen().getItem());
        if (target == null) return false;

        MutationHandler mutationHandler = new MutationHandler(input.base());
        if (mutationHandler.has(target)) {
            return false;
        }

        // The Maw's stage comes from its item type; a Maw below the mutation's stage cannot take it.
        if (!(input.base().getItem() instanceof MawItem)) {
            return false;
        }

        if (new StageHandler(input.base()).getStage().getId() < target.stage().getId()) {
            return false;
        }

        boolean isMutatable = target.ability().isMutatable(input.base(), level);

        if (!isMutatable) return false;

        return true;
    }

    /**
     * Mutating a Maw needs registries (some abilities apply enchantments in onCraft), which 26.1's
     * {@code assemble(input)} no longer receives. The Matrix uses {@link #assemble(MutationRecipeInput, HolderLookup.Provider)}.
     */
    @Override
    public ItemStack assemble(MutationRecipeInput input) {
        throw new UnsupportedOperationException("Mutation recipes need registries, use assemble(input, provider)");
    }

    public ItemStack assemble(MutationRecipeInput input, HolderLookup.Provider provider) {
        Mutations target = Mutations.byMutagen(input.mutagen().getItem());

        ItemStack baseCopy = input.base().copy();

        MutationHandler mutationHandler = new MutationHandler(baseCopy);

        mutationHandler.add(target);

        target.ability().onCraft(mutationHandler.getStack(), provider);

        return mutationHandler.getStack();
    }

    /** The mutated Maw this recipe displays as its result (first accepted base and mutagen). */
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        ItemStack baseStack = this.base.items().findFirst().map(item -> new ItemStack(item.value())).orElse(ItemStack.EMPTY);
        if (baseStack.isEmpty()) return ItemStack.EMPTY;

        Mutations target = this.mutagen.items().findFirst().map(item -> Mutations.byMutagen(item.value())).orElse(null);
        if (target == null) return baseStack;

        MutationHandler handler = new MutationHandler(baseStack);
        handler.add(target);

        target.ability().onCraft(handler.getStack(), provider);

        return handler.getStack();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<MutationRecipe> getSerializer() {
        return MutationRecipeSerializer.SERIALIZER;
    }

    @Override
    public RecipeType<MutationRecipe> getType() {
        return RavenousRecipes.MUTATION_RECIPE_TYPE.get();
    }

    /** Only the Mutation Matrix runs this recipe, so it never takes part in recipe-book placement. */
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public Ingredient getBase() {
        return base;
    }

    public Ingredient getFeast() {
        return feast;
    }

    public Ingredient getMutagen() {
        return mutagen;
    }
}
