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

public class SmithingRecipe extends AbstractSimpleRecipe<SmithingRecipe, Container, UpgradeRecipe> {
    protected Ingredient addon;

    protected SmithingRecipe(ResourceLocation id, ItemLike output) {
        super(id, RecipeType.SMITHING, output);
    }


    public SmithingRecipe setBase(ItemLike in) {
        return super.setInput(in);
    }

    public SmithingRecipe setBase(TagKey<Item> in) {
        return super.setInput(in);
    }

    public SmithingRecipe setAddon(ItemLike in) {
        this.addon = Ingredient.of(in);
        unlockedBy(in);
        return this;
    }

    public SmithingRecipe setAddon(TagKey<Item> in) {
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
