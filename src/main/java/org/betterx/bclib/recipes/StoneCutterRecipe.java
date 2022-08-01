package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.PathConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;

public class StoneCutterRecipe extends AbstractAdvancementRecipe {
    private final ResourceLocation id;
    String group;
    Ingredient input;
    ItemLike output;
    int count;
    boolean exist;


    StoneCutterRecipe(ResourceLocation id, ItemLike output) {
        this.id = id;
        this.group = "";
        this.exist = true;
        this.count = 1;
        this.output = output;
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
        this.input = Ingredient.of(in);
        unlockedBy(in);
        return this;
    }

    public StoneCutterRecipe setInput(TagKey<Item> in) {
        this.input = Ingredient.of(in);
        unlockedBy(in);
        return this;
    }

    public StoneCutterRecipe setGroup(String group) {
        this.group = group;
        return this;
    }

    public StoneCutterRecipe setOutputCount(int count) {
        this.count = count;
        return this;
    }

    public StoneCutterRecipe checkConfig(PathConfig config) {
        exist &= config.getBoolean("stonecutting", id.getPath(), true);
        return this;
    }

    public void build() {
        if (!exist) {
            BCLib.LOGGER.warning("Unable to build Recipe " + id);
            return;
        }

        if (input == null || input.isEmpty()) {
            BCLib.LOGGER.warning("Unable to build Recipe " + id + ": Empty Material List");
            return;
        }
        StonecutterRecipe recipe = new StonecutterRecipe(id, group, input, new ItemStack(output, count));
        BCLRecipeManager.addRecipe(RecipeType.STONECUTTING, recipe);
        registerAdvancement(recipe);
    }
}
