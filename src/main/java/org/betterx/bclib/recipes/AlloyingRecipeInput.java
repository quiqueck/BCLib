package org.betterx.bclib.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import org.jetbrains.annotations.NotNull;

public record AlloyingRecipeInput(ItemStack first, ItemStack second) implements RecipeInput {
    public AlloyingRecipeInput(ItemStack first) {
        this(first, ItemStack.EMPTY);
    }

    public AlloyingRecipeInput(ItemStack first, ItemStack second) {
        this.first = first == null ? ItemStack.EMPTY : first;
        this.second = second == null ? ItemStack.EMPTY : second;
    }

    public ItemStack any() {
        return first.isEmpty() ? second : first;
    }

    public void shrinkBoth() {
        this.first.shrink(1);
        this.second.shrink(1);
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        if (i == 0) return first;
        if (i == 1) return second;

        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 2;
    }
}
