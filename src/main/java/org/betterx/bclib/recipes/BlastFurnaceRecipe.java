package org.betterx.bclib.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

public class BlastFurnaceRecipe extends CookingRecipe<BlastFurnaceRecipe, Container, BlastingRecipe> {
    BlastFurnaceRecipe(ResourceLocation id, ItemLike output) {
        super(id, RecipeType.BLASTING, output);
    }

    public static BlastFurnaceRecipe make(String modID, String name, ItemLike output) {
        return make(new ResourceLocation(modID, name), output);
    }

    public static BlastFurnaceRecipe make(ResourceLocation id, ItemLike output) {
        BlastFurnaceRecipe res = new BlastFurnaceRecipe(id, output);
        res.createAdvancement(id, false);
        return res;
    }

    @Override
    protected BlastingRecipe buildRecipe() {
        return new BlastingRecipe(id, group, input, new ItemStack(output, count), experience, cookingTime);
    }
}
