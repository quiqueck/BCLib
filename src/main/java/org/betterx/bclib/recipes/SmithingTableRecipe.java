package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.ItemLike;

public class SmithingTableRecipe extends AbstractSimpleRecipe<SmithingTableRecipe, Container, UpgradeRecipe> {
    protected Ingredient addon;

    protected SmithingTableRecipe(ResourceLocation id, ItemLike output) {
        super(id, RecipeType.SMITHING, output);
    }


    static SmithingTableRecipe make(ResourceLocation id, ItemLike output) {
        SmithingTableRecipe res = new SmithingTableRecipe(id, output);
        res.createAdvancement(id, false);
        return res;
    }

    public SmithingTableRecipe setBase(ItemLike in) {
        return super.setInput(in);
    }

    public SmithingTableRecipe setBase(TagKey<Item> in) {
        return super.setInput(in);
    }

    public SmithingTableRecipe setAddition(ItemLike in) {
        this.exist &= BCLRecipeManager.exists(in);
        this.addon = Ingredient.of(in);
        unlockedBy(in);
        return this;
    }

    public SmithingTableRecipe setAddition(TagKey<Item> in) {
        this.addon = Ingredient.of(in);
        unlockedBy(in);
        return this;
    }

    @Override
    protected boolean hasErrors() {
        if (addon == null || addon.isEmpty()) {
            BCLib.LOGGER.warning("Unable to build Recipe " + id + ": No Addon Ingredient");
            return true;
        }
        return super.hasErrors();
    }

    @Override
    protected UpgradeRecipe buildRecipe() {
        return new UpgradeRecipe(id, input, addon, new ItemStack(output, count));
    }
}
