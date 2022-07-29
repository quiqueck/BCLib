package org.betterx.bclib.integration.emi;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.recipes.AlloyingRecipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeManager;

import dev.emi.emi.api.EmiRegistry;

public class EMIAlloyingRecipe extends EMIAbstractAlloyingRecipe<Container, AlloyingRecipe> {
    public EMIAlloyingRecipe(AlloyingRecipe recipe) {
        super(recipe, recipe.getId(), 1, false);
    }

    @Override
    protected int getSmeltTime() {
        return recipe.getSmeltTime();
    }

    @Override
    protected float getExperience() {
        return recipe.getExperience();
    }

    static void addAllRecipes(EmiRegistry emiRegistry, RecipeManager manager) {
        EMIPlugin.addAllRecipes(
                emiRegistry, manager, BCLib.LOGGER,
                AlloyingRecipe.TYPE, EMIAlloyingRecipe::new
        );
    }
}