package org.betterx.bclib.recipes;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

public class BCLRecipeBuilder {
    public static AlloyingRecipe.Builder alloying(Identifier id, ItemLike output) {
        return AlloyingRecipe.Builder.create(id, output);
    }

    public static AnvilRecipe.Builder anvil(Identifier id, ItemLike output) {
        return AnvilRecipe.create(id, output);
    }
}
