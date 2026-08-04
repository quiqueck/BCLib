package org.betterx.bclib.interfaces;


import org.betterx.bclib.recipes.AnvilRecipe;

import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public interface AnvilScreenHandlerExtended {
    void bcl_updateCurrentRecipe(RecipeHolder<AnvilRecipe> recipe);

    RecipeHolder<AnvilRecipe> bcl_getCurrentRecipe();

    List<RecipeHolder<AnvilRecipe>> bcl_getRecipes();

    /**
     * The number of anvil recipes that match the current input.
     * <p>
     * Unlike {@link #bcl_getRecipes()} this is synced to the client, which has no access to the
     * recipes themselves since 1.21.2.
     */
    int bcl_getRecipeCount();

    default void be_nextRecipe() {
        List<RecipeHolder<AnvilRecipe>> recipes = bcl_getRecipes();
        if (recipes.size() < 2) return;
        RecipeHolder<AnvilRecipe> current = bcl_getCurrentRecipe();
        int i = recipes.indexOf(current) + 1;
        if (i >= recipes.size()) {
            i = 0;
        }
        bcl_updateCurrentRecipe(recipes.get(i));
    }

    default void be_previousRecipe() {
        List<RecipeHolder<AnvilRecipe>> recipes = bcl_getRecipes();
        if (recipes.size() < 2) return;
        RecipeHolder<AnvilRecipe> current = bcl_getCurrentRecipe();
        int i = recipes.indexOf(current) - 1;
        if (i < 0) {
            i = recipes.size() - 1;
        }
        bcl_updateCurrentRecipe(recipes.get(i));
    }
}
