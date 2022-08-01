package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.PathConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.ItemLike;

public class SmithingTableRecipe extends AbstractAdvancementRecipe {

    private final static SmithingTableRecipe BUILDER = new SmithingTableRecipe();
    private final static RecipeType<UpgradeRecipe> TYPE = RecipeType.SMITHING;

    public static SmithingTableRecipe create(String modID, String name) {
        return create(new ResourceLocation(modID, name));
    }

    public static SmithingTableRecipe create(ResourceLocation id) {
        BUILDER.id = id;
        BUILDER.base = null;
        BUILDER.addition = null;
        BUILDER.result = null;
        BUILDER.exist = true;
        BUILDER.createAdvancement(id, false);
        return BUILDER;
    }

    private ResourceLocation id;
    private Ingredient base;
    private Ingredient addition;
    private ItemStack result;
    private boolean exist;

    private SmithingTableRecipe() {
    }

    public SmithingTableRecipe checkConfig(PathConfig config) {
        exist &= config.getBoolean("smithing", id.getPath(), true);
        return this;
    }

    public SmithingTableRecipe setResult(ItemLike item) {
        return this.setResult(item, 1);
    }

    public SmithingTableRecipe setResult(ItemLike item, int count) {
        this.exist &= BCLRecipeManager.exists(item);
        this.result = new ItemStack(item, count);
        return this;
    }

    public SmithingTableRecipe setBase(ItemLike... items) {
        unlockedBy(items);
        this.exist &= BCLRecipeManager.exists(items);
        this.base = Ingredient.of(items);
        return this;
    }

    public SmithingTableRecipe setBase(TagKey<Item> tag) {
        unlockedBy(tag);
        this.base = (Ingredient.of(tag));
        return this;
    }

    public SmithingTableRecipe setAddition(ItemLike... items) {
        unlockedBy(items);
        this.exist &= BCLRecipeManager.exists(items);
        this.addition = Ingredient.of(items);
        return this;
    }

    public SmithingTableRecipe setAddition(TagKey<Item> tag) {
        unlockedBy(tag);
        this.addition = (Ingredient.of(tag));
        return this;
    }

    public void build() {
        if (!exist) {
            return;
        }

        if (base == null) {
            BCLib.LOGGER.warning("Base input for Smithing recipe can't be 'null', recipe {} will be ignored!", id);
            return;
        }
        if (addition == null) {
            BCLib.LOGGER.warning("Addition input for Smithing recipe can't be 'null', recipe {} will be ignored!", id);
            return;
        }
        if (result == null) {
            BCLib.LOGGER.warning("Result for Smithing recipe can't be 'null', recipe {} will be ignored!", id);
            return;
        }
        if (BCLRecipeManager.getRecipe(TYPE, id) != null) {
            BCLib.LOGGER.warning("Can't add Smithing recipe! Id {} already exists!", id);
            return;
        }

        UpgradeRecipe recipe = new UpgradeRecipe(id, base, addition, result);
        BCLRecipeManager.addRecipe(TYPE, recipe);
        registerAdvancement(recipe);
    }
}
