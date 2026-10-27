package net.rywir.ravenousmaw.content.recipe.matrix;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.content.item.MawItem;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousRecipes;
import net.rywir.ravenousmaw.system.MutationHandler;

public class MutationRecipe implements Recipe<MutationRecipeInput> {
    private final Ingredient base;
    private final Ingredient feast;
    private final Ingredient mutagen;

    public MutationRecipe(Ingredient base, Ingredient feast, Ingredient mutagen) {
        this.base = base;
        this.feast = feast;
        this.mutagen = mutagen;
    }

    public MutationRecipe(MutationRecipeInput input) {
        this(Ingredient.of(input.base()), Ingredient.of(input.feast()), Ingredient.of(input.mutagen()));
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

        if (input.base().getItem() instanceof MawItem maw) {
            if (maw.getStage().getId() < target.stage().getId()) {
                return false;
            }
        } else {
            return false;
        }

        boolean isMutatable = target.ability().isMutatable(input.base(), level);

        if (!isMutatable) return false;

        return true;
    }

    @Override
    public ItemStack assemble(MutationRecipeInput input, HolderLookup.Provider provider) {
        Mutations target = Mutations.byMutagen(input.mutagen().getItem());

        ItemStack baseCopy = input.base().copy();

        MutationHandler mutationHandler = new MutationHandler(baseCopy);

        mutationHandler.add(target);

        target.ability().onCraft(mutationHandler.getStack());

        return mutationHandler.getStack();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        ItemStack baseStack = this.base.getItems().length > 0 ? this.base.getItems()[0].copy() : ItemStack.EMPTY;
        if (baseStack.isEmpty()) return ItemStack.EMPTY;

        Mutations target = Mutations.byMutagen(mutagen.getItems()[0].getItem());
        if (target == null) return baseStack;

        MutationHandler handler = new MutationHandler(baseStack);
        handler.add(target);

        target.ability().onCraft(handler.getStack());

        return handler.getStack();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RavenousRecipes.MUTATION_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RavenousRecipes.MUTATION_RECIPE_TYPE.get();
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
