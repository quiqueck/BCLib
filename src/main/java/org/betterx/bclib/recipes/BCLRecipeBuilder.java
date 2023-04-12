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

    public static CookingRecipeBuilder blasting(ResourceLocation id, ItemLike output) {
        return CookingRecipeBuilder.make(id, output).disableSmelter().enableBlastFurnace();
    }

    public static CraftingRecipeBuilder crafting(ResourceLocation id, ItemLike output) {
        return CraftingRecipeBuilder.make(id, output);
    }

    public static CookingRecipeBuilder smelting(ResourceLocation id, ItemLike output) {
        return CookingRecipeBuilder.make(id, output);
    }

    public static SmithingRecipeBuilder smithing(ResourceLocation id, ItemLike output) {
        return SmithingRecipeBuilder.make(id, output);
    }

    public static StonecutterRecipeBuilder stonecutting(ResourceLocation id, ItemLike output) {
        return StonecutterRecipeBuilder.make(id, output);
    }
}
