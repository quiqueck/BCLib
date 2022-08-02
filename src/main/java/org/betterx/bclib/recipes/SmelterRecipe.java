package org.betterx.bclib.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;

public class SmelterRecipe extends CookingRecipe<SmelterRecipe, Container, SmeltingRecipe> {
    SmelterRecipe(
            ResourceLocation id, ItemLike output
    ) {
        super(id, RecipeType.SMELTING, output);
    }

    public static SmelterRecipe make(String modID, String name, ItemLike output) {
        return make(new ResourceLocation(modID, name), output);
    }

    public static SmelterRecipe make(ResourceLocation id, ItemLike output) {
        SmelterRecipe res = new SmelterRecipe(id, output);
        res.createAdvancement(id, false);
        return res;
    }

    @Override
    protected SmeltingRecipe buildRecipe() {
        return new SmeltingRecipe(id, group, input, new ItemStack(output, count), experience, cookingTime);
    }
}
