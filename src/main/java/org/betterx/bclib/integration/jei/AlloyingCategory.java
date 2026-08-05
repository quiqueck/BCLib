package org.betterx.bclib.integration.jei;

import org.betterx.bclib.interfaces.AlloyingRecipeWorkstation;
import org.betterx.bclib.recipes.AlloyingRecipe;
import org.betterx.bclib.recipes.AlloyingRecipeInput;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;

import java.util.List;
import java.util.Optional;

public class AlloyingCategory implements IRecipeCategory<RecipeHolder<AlloyingRecipe>> {
    public static final IRecipeHolderType<AlloyingRecipe> TYPE = IRecipeHolderType.create(AlloyingRecipe.TYPE);

    private static final int WIDTH = 100;
    private static final int HEIGHT = 42;

    private final IDrawable icon;
    private final Component title;

    public AlloyingCategory(IGuiHelper guiHelper) {
        List<Block> workstations = AlloyingRecipeWorkstation.getWorkstations();
        this.icon = guiHelper.createDrawableItemStack(AlloyingRecipeWorkstation.getWorkstationIcon());
        this.title = workstations.isEmpty()
                ? Component.translatable("emi.category.bclib.alloying")
                : Component.translatable(workstations.get(0).getDescriptionId());
    }

    @Override
    public IRecipeHolderType<AlloyingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            RecipeHolder<AlloyingRecipe> recipeHolder,
            IFocusGroup focuses
    ) {
        AlloyingRecipe recipe = recipeHolder.value();
        List<Optional<Ingredient>> ingredients = recipe.getIngredients();

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
               .add(ingredients.get(0).orElseThrow());

        if (ingredients.size() > 1 && ingredients.get(1).isPresent()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 20, 1)
                   .add(ingredients.get(1).get());
        }

        ItemStack output = recipe.assemble(new AlloyingRecipeInput(ItemStack.EMPTY));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 78, 4)
               .add(output);
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder,
            RecipeHolder<AlloyingRecipe> recipeHolder,
            IFocusGroup focuses
    ) {
        AlloyingRecipe recipe = recipeHolder.value();

        builder.addAnimatedRecipeArrow(recipe.getSmeltTime()).setPosition(43, 2);
        builder.addAnimatedRecipeFlame(200).setPosition(1, 20);
        builder.addText(
                List.of(Component.translatable(
                        "tooltip.bclib.jei.alloying_info",
                        recipe.experience(),
                        recipe.getSmeltTime() / 20.0
                )),
                WIDTH, 9
        ).setPosition(0, HEIGHT - 9).setColor(0xFF808080);
    }
}
