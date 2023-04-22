package org.betterx.bclib.interfaces;


import org.betterx.bclib.recipes.AnvilRecipe;

import java.util.List;

public interface AnvilScreenHandlerExtended {
    void bcl_updateCurrentRecipe(AnvilRecipe recipe);

    AnvilRecipe bcl_getCurrentRecipe();

    List<AnvilRecipe> bcl_getRecipes();

    default void be_nextRecipe() {
        List<AnvilRecipe> recipes = bcl_getRecipes();
        if (recipes.size() < 2) return;
        AnvilRecipe current = bcl_getCurrentRecipe();
        int i = recipes.indexOf(current) + 1;
        if (i >= recipes.size()) {
            i = 0;
        }
        bcl_updateCurrentRecipe(recipes.get(i));
    }

    default void be_previousRecipe() {
        List<AnvilRecipe> recipes = bcl_getRecipes();
        if (recipes.size() < 2) return;
        AnvilRecipe current = bcl_getCurrentRecipe();
        int i = recipes.indexOf(current) - 1;
        if (i <= 0) {
            i = recipes.size() - 1;
        }
        bcl_updateCurrentRecipe(recipes.get(i));
    }
}
