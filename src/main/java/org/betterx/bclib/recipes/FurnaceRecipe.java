package org.betterx.bclib.recipes;

import org.betterx.bclib.config.PathConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

public class FurnaceRecipe extends AbstractAdvancementRecipe {
    private static final FurnaceRecipe INSTANCE = new FurnaceRecipe();

    private ResourceLocation id;
    private Ingredient input;
    private ItemLike output;
    private boolean exist;
    private String group;
    private int count;
    private int time;
    private float xp;
    protected CookingBookCategory bookCategory;

    private FurnaceRecipe() {
    }

    static FurnaceRecipe make(ResourceLocation id, ItemLike output) {
        INSTANCE.id = id;
        INSTANCE.group = "";
        INSTANCE.input = null;
        INSTANCE.output = output;
        INSTANCE.count = 1;
        INSTANCE.time = 200;
        INSTANCE.xp = 0;
        INSTANCE.exist = BCLRecipeManager.exists(output);
        INSTANCE.createAdvancement(INSTANCE.id, false);
        INSTANCE.bookCategory = CookingBookCategory.MISC;

        return INSTANCE;
    }

    public FurnaceRecipe setInput(ItemLike input) {
        exist &= BCLRecipeManager.exists(input);
        this.input = Ingredient.of(input);
        unlockedBy(input);
        return this;
    }

    public FurnaceRecipe setInput(TagKey<Item> tag) {
        this.input = Ingredient.of(tag);
        unlockedBy(tag);
        return this;
    }

    public FurnaceRecipe checkConfig(PathConfig config) {
        exist &= config.getBoolean("furnace", id.getPath(), true);
        return this;
    }

    public FurnaceRecipe setGroup(String group) {
        this.group = group;
        return this;
    }

    public FurnaceRecipe setOutputCount(int count) {
        this.count = count;
        return this;
    }

    public FurnaceRecipe setExperience(float xp) {
        this.xp = xp;
        return this;
    }

    public FurnaceRecipe setCookingTime(int time) {
        this.time = time;
        return this;
    }

    public FurnaceRecipe setCookingBookCategory(CookingBookCategory c) {
        bookCategory = c;
        return this;
    }

    public void build() {
        build(false, false, false);
    }

    public void buildWithBlasting() {
        build(true, false, false);
    }

    public void buildFoodlike() {
        build(false, true, true);
    }

    public void build(boolean blasting, boolean campfire, boolean smoker) {
        if (!exist) {
            return;
        }

        SmeltingRecipe recipe = new SmeltingRecipe(
                new ResourceLocation(id + "_smelting"),
                group,
                bookCategory,
                input,
                new ItemStack(output, count),
                xp,
                time
        );
        BCLRecipeManager.addRecipe(RecipeType.SMELTING, recipe);
        registerAdvancement(recipe);

        if (blasting) {
            BlastingRecipe recipe2 = new BlastingRecipe(
                    new ResourceLocation(id + "_blasting"),
                    group,
                    bookCategory,
                    input,
                    new ItemStack(output, count),
                    xp,
                    time / 2
            );

            BCLRecipeManager.addRecipe(RecipeType.BLASTING, recipe2);
        }

        if (campfire) {
            CampfireCookingRecipe recipe2 = new CampfireCookingRecipe(
                    new ResourceLocation(id + "_campfire"),
                    group,
                    bookCategory,
                    input,
                    new ItemStack(output, count),
                    xp,
                    time * 3
            );

            BCLRecipeManager.addRecipe(RecipeType.CAMPFIRE_COOKING, recipe2);
        }

        if (smoker) {
            SmokingRecipe recipe2 = new SmokingRecipe(
                    new ResourceLocation(id + "_smoker"),
                    group,
                    bookCategory,
                    input,
                    new ItemStack(output, count),
                    xp,
                    time / 2
            );

            BCLRecipeManager.addRecipe(RecipeType.SMOKING, recipe2);
        }
    }
}
