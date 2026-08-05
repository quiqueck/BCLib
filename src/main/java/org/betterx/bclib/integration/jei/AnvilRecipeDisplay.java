package org.betterx.bclib.integration.jei;

import org.betterx.bclib.recipes.AnvilRecipe;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * One JEI display entry per (recipe, applicable hammer) pair, since a single {@link AnvilRecipe}
 * may be craftable with several different hammer items (see {@link AnvilRecipe#getAllHammers()}).
 *
 * <p>{@code recipeId} is carried through (rather than just the bare {@link AnvilRecipe}) so
 * {@link AnvilCategory#getIdentifier} can give JEI a stable per-display identifier - without one,
 * JEI can't bookmark these recipes.
 */
public record AnvilRecipeDisplay(Identifier recipeId, AnvilRecipe recipe, Item hammer) {
}
