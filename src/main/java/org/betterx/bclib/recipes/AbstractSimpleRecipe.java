package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.PathConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

public abstract class AbstractSimpleRecipe<T extends AbstractSimpleRecipe, C extends Container, R extends Recipe<C>> extends AbstractAdvancementRecipe {
    public final ResourceLocation id;
    protected String group;
    protected Ingredient input;
    protected ItemLike output;
    protected final String category;
    protected final RecipeType<R> type;
    protected int count;
    protected boolean exist;


    protected AbstractSimpleRecipe(ResourceLocation id, RecipeType<R> type, ItemLike output) {
        this(id, type, type.toString(), output);
    }

    protected AbstractSimpleRecipe(ResourceLocation id, RecipeType<R> type, String category, ItemLike output) {
        this.id = id;
        this.group = "";
        this.exist = true;
        this.count = 1;
        this.output = output;

        this.category = category;
        this.type = type;
    }


    protected T setInput(ItemLike in) {
        this.input = Ingredient.of(in);
        unlockedBy(in);
        return (T) this;
    }

    protected T setInput(TagKey<Item> in) {
        this.input = Ingredient.of(in);
        unlockedBy(in);
        return (T) this;
    }

    public T setGroup(String group) {
        this.group = group;
        return (T) this;
    }

    public T setOutputCount(int count) {
        this.count = count;
        return (T) this;
    }

    public T checkConfig(PathConfig config) {
        exist &= config.getBoolean(category, id.getPath(), true);
        return (T) this;
    }

    protected abstract R buildRecipe();

    protected boolean hasErrors() {
        return false;
    }

    public final void build() {
        if (!exist) {
            BCLib.LOGGER.warning("Unable to build Recipe " + id);
            return;
        }

        if (input == null || input.isEmpty()) {
            BCLib.LOGGER.warning("Unable to build Recipe " + id + ": No Input Material");
            return;
        }

        if (hasErrors()) return;

        R recipe = buildRecipe();
        BCLRecipeManager.addRecipe(type, recipe);
        registerAdvancement(recipe);
    }
}
