package org.betterx.bclib.integration.jei;

import org.betterx.bclib.recipes.AlloyingRecipe;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;

import java.util.List;

/**
 * {@link AlloyingRecipe}s (e.g. BetterEnd's End Stone Smelter): two inputs in either order, one
 * output, laid out like JEI's own furnace categories.
 */
@Environment(EnvType.CLIENT)
public class AlloyingCategory extends AbstractRecipeCategory<RecipeHolder<AlloyingRecipe>> {
    public static final IRecipeHolderType<AlloyingRecipe> TYPE = IRecipeHolderType.create(AlloyingRecipe.TYPE);

    private static final int WIDTH = 100;
    private static final int HEIGHT = 54;
    private static final int TEXT_COLOR = 0xFF808080;

    public AlloyingCategory(IGuiHelper guiHelper, ItemLike icon) {
        super(
                TYPE,
                Component.translatable("category.bclib.jei.alloying"),
                guiHelper.createDrawableItemLike(icon),
                WIDTH,
                HEIGHT
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<AlloyingRecipe> holder, IFocusGroup focuses) {
        final AlloyingRecipe recipe = holder.value();
        // placementInfo() leaves out a missing second input, getIngredients() would not
        final List<Ingredient> inputs = recipe.placementInfo().ingredients();

        for (int i = 0; i < 2; i++) {
            final IRecipeSlotBuilder slot = builder.addInputSlot(1 + 18 * i, 1).setStandardSlotBackground();
            if (i < inputs.size()) slot.add(inputs.get(i));
        }

        builder.addOutputSlot(WIDTH - 22, 19)
               .setOutputSlotBackground()
               .add(recipe.assemble(null, null));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<AlloyingRecipe> holder, IFocusGroup focuses) {
        final AlloyingRecipe recipe = holder.value();
        final int smeltTime = recipe.getSmeltTime();

        builder.addAnimatedRecipeFlame(300).setPosition(11, 20);
        builder.addAnimatedRecipeArrow(Math.max(1, smeltTime)).setPosition(44, 18);

        if (recipe.experience() > 0) {
            builder.addText(Component.translatable("gui.jei.category.smelting.experience", recipe.experience()), WIDTH - 40, 10)
                   .setPosition(0, 0, WIDTH, HEIGHT, HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
                   .setTextAlignment(HorizontalAlignment.RIGHT)
                   .setColor(TEXT_COLOR);
        }
        if (smeltTime > 0) {
            builder.addText(Component.translatable("gui.jei.category.smelting.time.seconds", smeltTime / 20), WIDTH - 40, 10)
                   .setPosition(0, 0, WIDTH, HEIGHT, HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)
                   .setTextAlignment(HorizontalAlignment.RIGHT)
                   .setTextAlignment(VerticalAlignment.BOTTOM)
                   .setColor(TEXT_COLOR);
        }
    }

    @Override
    public Codec<RecipeHolder<AlloyingRecipe>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
        return codecHelper.getRecipeHolderCodec();
    }
}
