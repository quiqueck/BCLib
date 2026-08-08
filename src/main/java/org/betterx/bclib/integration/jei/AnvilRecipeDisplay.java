package org.betterx.bclib.integration.jei;

import org.betterx.bclib.recipes.AnvilRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * One JEI display entry per (recipe, applicable hammer) pair, since a single {@link AnvilRecipe}
 * may be craftable with several different hammer items (see
 * {@link AnvilRecipe#getAllHammers(net.minecraft.core.HolderLookup.Provider)}).
 *
 * <p>{@code recipeId} is carried through (rather than just the bare {@link AnvilRecipe}) so
 * {@link AnvilCategory#getRegistryName} can give JEI a stable per-display identifier - without one,
 * JEI can't bookmark these recipes.
 */
public record AnvilRecipeDisplay(ResourceLocation recipeId, AnvilRecipe recipe, Item hammer) {
}
