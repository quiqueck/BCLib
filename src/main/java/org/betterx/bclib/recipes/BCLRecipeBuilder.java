package org.betterx.bclib.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class BCLRecipeBuilder {
    public static AlloyingRecipe.Builder alloying(ResourceLocation id, ItemLike output) {
        return AlloyingRecipe.Builder.create(id, output);
    }

    public static AnvilRecipe.Builder anvil(ResourceLocation id, ItemLike output) {
        return AnvilRecipe.create(id, output);
    }
}
