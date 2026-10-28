package net.rywir.ravenousmaw.compat.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.recipe.matrix.MutationRecipe;
import net.rywir.ravenousmaw.registry.RavenousBlocks;

public class MutationMatrixRecipeCategory implements IRecipeCategory<MutationRecipe> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "mutation_matrix");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "textures/gui/mutation_matrix.png");

    public static final IRecipeType<MutationRecipe> MUTATION_RECIPE_RECIPE_TYPE = IRecipeType.create(UID, MutationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public MutationMatrixRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 7, 16, 162, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(RavenousBlocks.MUTATION_MATRIX));
        this.arrow = helper.createDrawable(Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "textures/gui/progress_arrow.png"), 0, 0, 24, 16);
    }

    @Override
    public IRecipeType<MutationRecipe> getRecipeType() {
        return MUTATION_RECIPE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.ravenousmaw.mutation_matrix");
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MutationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 19).add(recipe.getBase());
        builder.addSlot(RecipeIngredientRole.INPUT, 41, 19).add(recipe.getFeast());
        builder.addSlot(RecipeIngredientRole.INPUT, 59, 19).add(recipe.getMutagen());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 19).add(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }

    @Override
    public void draw(MutationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);
        arrow.draw(graphics, 82, 19);
        background.draw(graphics);
    }
}
