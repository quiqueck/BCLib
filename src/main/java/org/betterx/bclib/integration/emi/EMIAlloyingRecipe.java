package org.betterx.bclib.integration.emi;

import org.betterx.bclib.recipes.AlloyingRecipe;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;

public class EMIAlloyingRecipe implements EmiRecipe {
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;
    private final AlloyingRecipe recipe;

    private final int fuelMultiplier;
    private final boolean infiniBurn;

    public EMIAlloyingRecipe(AlloyingRecipe recipe) {
        this.recipe = recipe;

        this.input = List.of(
                EmiIngredient.of(recipe.getIngredients().get(0)),
                EmiIngredient.of(recipe.getIngredients().get(1))
        );
        this.output = List.of(EmiStack.of(recipe.getResultItem()));
        fuelMultiplier = 1;
        infiniBurn = false;
    }

    static void addAllRecipes(EmiRegistry emiRegistry, RecipeManager manager) {
        for (AlloyingRecipe recipe : manager.getAllRecipesFor(AlloyingRecipe.TYPE)) {
            emiRegistry.addRecipe(new EMIAlloyingRecipe(recipe));
        }
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EMIPlugin.END_ALLOYING_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return recipe.getId();
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
        return 82 + 22;
    }

    @Override
    public int getDisplayHeight() {
        return 38;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add an arrow texture to indicate processing
        widgets.addFillingArrow(46, 5, 50 * this.recipe.getSmeltTime()).tooltip((mx, my) -> {
            return List.of(ClientTooltipComponent.create(EmiPort.ordered(EmiPort.translatable(
                    "emi.cooking.time",
                    new Object[]{(float) this.recipe.getSmeltTime() / 20.0F}
            ))));
        });

        if (this.infiniBurn) {
            widgets.addTexture(EmiTexture.FULL_FLAME, 1, 24);
        } else {
            widgets.addTexture(EmiTexture.EMPTY_FLAME, 1, 24);
            widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 1, 24, 4000 / this.fuelMultiplier, false, true, true);
        }

        // Adds an input slot on the left
        widgets.addSlot(input.get(0), 0, 4);
        widgets.addSlot(input.get(1), 20, 4);
        widgets.addText(EmiPort.ordered(EmiPort.translatable(
                "emi.cooking.experience",
                new Object[]{this.recipe.getExperience()}
        )), 46, 28, -1, true);

        // Adds an output slot on the right
        // Note that output slots need to call `recipeContext` to inform EMI about their recipe context
        // This includes being able to resolve recipe trees, favorite stacks with recipe context, and more
        widgets.addSlot(output.get(0), 78, 0).output(true).recipeContext(this);
    }

    @Override
    public boolean supportsRecipeTree() {
        return true;
    }
}
