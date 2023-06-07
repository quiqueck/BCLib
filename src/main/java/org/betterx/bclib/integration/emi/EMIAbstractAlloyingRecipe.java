package org.betterx.bclib.integration.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.List;

public abstract class EMIAbstractAlloyingRecipe<C extends Container, T extends Recipe<C>> implements EmiRecipe {
    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;
    protected final T recipe;

    private final int fuelMultiplier;
    private final boolean infiniBurn;

    public EMIAbstractAlloyingRecipe(T recipe, ResourceLocation id, int fuelMultiplier, boolean infiniBurn) {
        this.recipe = recipe;
        this.id = id;
        this.input = List.of(
                EmiIngredient.of(recipe.getIngredients().get(0)),
                recipe.getIngredients().size() > 1
                        ? EmiIngredient.of(recipe.getIngredients().get(1))
                        : EmiIngredient.of(Ingredient.EMPTY)
        );

        this.output = List.of(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
        this.fuelMultiplier = fuelMultiplier;
        this.infiniBurn = infiniBurn;
    }

    protected abstract int getSmeltTime();
    protected abstract float getExperience();


    @Override
    public EmiRecipeCategory getCategory() {
        return EMIPlugin.END_ALLOYING_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
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

    protected int getXOffset() {
        return 22;
    }

    @Override
    public int getDisplayWidth() {
        return 82 + getXOffset();
    }

    @Override
    public int getDisplayHeight() {
        return 38;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add an arrow texture to indicate processing
        widgets.addFillingArrow(24 + getXOffset(), 5, 50 * getSmeltTime())
               .tooltip((mx, my) -> List.of(ClientTooltipComponent.create(Component.translatable(
                       "emi.cooking.time",
                       new Object[]{(float) getSmeltTime() / 20.0F}
               ).getVisualOrderText())));

        if (this.infiniBurn) {
            widgets.addTexture(EmiTexture.FULL_FLAME, 1, 24);
        } else {
            widgets.addTexture(EmiTexture.EMPTY_FLAME, 1, 24);
            widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 1, 24, 4000 / this.fuelMultiplier, false, true, true);
        }

        // Adds an input slot on the left
        widgets.addSlot(input.get(0), 0, 4);
        widgets.addSlot((input.size() > 1) ? input.get(1) : null, 20, 4);
        widgets.addText(
                Component.translatable("emi.cooking.experience", getExperience()).getVisualOrderText(),
                24 + getXOffset(), 28, 0xFFFFFFFF, true
        );

        // Adds an output slot on the right
        // Note that output slots need to call `recipeContext` to inform EMI about their recipe context
        // This includes being able to resolve recipe trees, favorite stacks with recipe context, and more
        widgets.addSlot(output.get(0), 56 + getXOffset(), 0).large(true).recipeContext(this);
    }

    @Override
    public boolean supportsRecipeTree() {
        return true;
    }
}

