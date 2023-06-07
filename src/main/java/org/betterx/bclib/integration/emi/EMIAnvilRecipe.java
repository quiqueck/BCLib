package org.betterx.bclib.integration.emi;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.recipes.AnvilRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Nullable;

public class EMIAnvilRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;
    private final EmiRecipeCategory category;

    public EMIAnvilRecipe(AnvilRecipe recipe, Item hammer) {
        this.id = new ResourceLocation(
                "emi",
                recipe.getId().getNamespace() + "/" + recipe.getId().getPath() + "/anvil/" + hammer.getDescriptionId()
        );
        this.input = List.of(
                EmiIngredient.of(recipe.getMainIngredient(), recipe.getInputCount()),
                EmiIngredient.of(Ingredient.of(hammer))
        );
        this.output = List.of(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
        this.category = EMIPlugin.getAnvilCategoryForLevel(recipe.getAnvilLevel());
    }

    static void addAllRecipes(EmiRegistry emiRegistry, RecipeManager manager) {
        Iterable<Holder<Item>> hammers = AnvilRecipe.getAllHammers();
        EMIPlugin.addAllRecipes(
                emiRegistry, manager, BCLib.LOGGER,
                AnvilRecipe.TYPE,
                recipe -> StreamSupport.stream(hammers.spliterator(), false)
                                       .map(Holder::value)
                                       .filter(recipe::canUse)
                                       .toList(),
                EMIAnvilRecipe::new
        );
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 104;
    }

    @Override
    public int getDisplayHeight() {
        return 26;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        // Add an arrow texture to indicate processing
        widgetHolder.addTexture(EmiTexture.EMPTY_ARROW, 46, 5);

        // Adds an input slot on the left
        widgetHolder.addSlot(input.get(0), 0, 4);
        widgetHolder.addSlot(input.get(1), 20, 4).catalyst(true);

        // Adds an output slot on the right
        // Note that output slots need to call `recipeContext` to inform EMI about their recipe context
        // This includes being able to resolve recipe trees, favorite stacks with recipe context, and more
        widgetHolder.addSlot(output.get(0), 78, 0).large(true).recipeContext(this);
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(input.get(1));
    }

    @Override
    public boolean supportsRecipeTree() {
        return true;
    }
}
