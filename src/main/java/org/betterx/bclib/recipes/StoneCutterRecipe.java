package org.betterx.bclib.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;

public class StoneCutterRecipe extends AbstractSimpleRecipe<StoneCutterRecipe, Container, StonecutterRecipe> {
    StoneCutterRecipe(ResourceLocation id, ItemLike output) {
        super(id, RecipeType.STONECUTTING, "stonecutting", output);
    }

    public static StoneCutterRecipe make(String modID, String name, ItemLike output) {
        return make(new ResourceLocation(modID, name), output);
    }

    public static StoneCutterRecipe make(ResourceLocation id, ItemLike output) {
        StoneCutterRecipe res = new StoneCutterRecipe(id, output);
        res.createAdvancement(id, false);
        return res;
    }

    public StoneCutterRecipe setInput(ItemLike in) {
        return super.setInput(in);
    }

    public StoneCutterRecipe setInput(TagKey<Item> in) {
        return super.setInput(in);
    }

    @Override
    protected StonecutterRecipe buildRecipe() {
        return new StonecutterRecipe(id, group, input, new ItemStack(output, count));
    }
}
