package org.betterx.bclib.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

public abstract class CookingRecipe<T extends AbstractSimpleRecipe, C extends Container, R extends Recipe<C>> extends AbstractSimpleRecipe<T, C, R> {
    protected float experience;
    protected int cookingTime;
    protected CookingBookCategory bookCategory;

    CookingRecipe(ResourceLocation id, RecipeType<R> type, ItemLike output) {
        this(id, type, type.toString(), output);
    }

    CookingRecipe(ResourceLocation id, RecipeType<R> type, String category, ItemLike output) {
        super(id, type, category, output);
        cookingTime = 1000;
        experience = 0;
        this.bookCategory = CookingBookCategory.MISC;
    }

    public T setInput(ItemLike in) {
        return super.setInput(in);
    }

    public T setInput(TagKey<Item> in) {
        return super.setInput(in);
    }

    public T setExperience(float xp) {
        experience = xp;
        return (T) this;
    }

    public T setCookingTime(int time) {
        cookingTime = time;
        return (T) this;
    }

    public T setCookingBookCategory(CookingBookCategory c) {
        bookCategory = c;
        return (T) this;
    }
}
