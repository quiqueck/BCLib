package org.betterx.bclib.integration.jei;

import org.betterx.bclib.BCLib;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;

import java.util.List;

public class AnvilCategory implements IRecipeCategory<AnvilRecipeDisplay> {
    private static final int WIDTH = 104;
    private static final int HEIGHT = 42;

    public final int level;
    private final IRecipeType<AnvilRecipeDisplay> type;
    private final IDrawable icon;
    private final Component title;

    public AnvilCategory(IGuiHelper guiHelper, int level, ItemLike workstation) {
        this.level = level;
        this.type = IRecipeType.create(BCLib.C.mk("anvil_" + level), AnvilRecipeDisplay.class);
        this.icon = guiHelper.createDrawableItemLike(workstation);
        this.title = Component.translatable("emi.category.bclib.anvil_" + level);
    }

    public IRecipeType<AnvilRecipeDisplay> getType() {
        return type;
    }

    @Override
    public IRecipeType<AnvilRecipeDisplay> getRecipeType() {
        return type;
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
    public Identifier getIdentifier(AnvilRecipeDisplay display) {
        // IRecipeCategory's default getIdentifier() only knows how to derive one from a
        // RecipeHolder; AnvilRecipeDisplay isn't one (it pairs one AnvilRecipe with one of
        // possibly several applicable hammers), so without this override JEI has no stable id to
        // bookmark these recipes by.
        Identifier hammerId = BuiltInRegistries.ITEM.getKey(display.hammer());
        return Identifier.fromNamespaceAndPath(
                display.recipeId().getNamespace(),
                display.recipeId().getPath() + "/" + hammerId.getNamespace() + "_" + hammerId.getPath()
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AnvilRecipeDisplay display, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 0, 4)
               .add(new ItemStack(display.hammer()));

        builder.addSlot(RecipeIngredientRole.INPUT, 21, 4)
               .add(display.recipe().getMainIngredient())
               .addRichTooltipCallback((view, tooltip) -> {
                   int count = display.recipe().getInputCount();
                   if (count > 1) {
                       tooltip.add(Component.literal("x" + count));
                   }
               });

        // AnvilRecipe.assemble() never reads its input argument, it just builds outputItem/outputCount.
        ItemStack output = display.recipe().assemble(null);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 4)
               .add(output);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AnvilRecipeDisplay display, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(45, 3);
        builder.addText(
                List.of(Component.translatable("tooltip.bclib.jei.anvil_damage", display.recipe().getDamage())),
                WIDTH, 9
        ).setPosition(0, HEIGHT - 9).setColor(0xFF808080);
    }
}
