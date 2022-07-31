package org.betterx.bclib.recipes;

import org.betterx.bclib.config.PathConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

public class FurnaceRecipe {
    private static final FurnaceRecipe INSTANCE = new FurnaceRecipe();

    private ResourceLocation id;
    private ItemLike input;
    private ItemLike output;
    private boolean exist;
    private String group;
    private int count;
    private int time;
    private float xp;

    private FurnaceRecipe() {
    }

    public static FurnaceRecipe make(String modID, String name, ItemLike input, ItemLike output) {
        INSTANCE.id = new ResourceLocation(modID, name);
        INSTANCE.group = "";
        INSTANCE.input = input;
        INSTANCE.output = output;
        INSTANCE.count = 1;
        INSTANCE.time = 200;
        INSTANCE.xp = 0;
        INSTANCE.exist = BCLRecipeManager.exists(output) && BCLRecipeManager.exists(input);
        return INSTANCE;
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

    public FurnaceRecipe setXP(float xp) {
        this.xp = xp;
        return this;
    }

    public FurnaceRecipe setCookTime(int time) {
        this.time = time;
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
                Ingredient.of(input),
                new ItemStack(output, count),
                xp,
                time
        );
        BCLRecipeManager.addRecipeAndCreateAdvancement(RecipeType.SMELTING, recipe, false);

        if (blasting) {
            BlastingRecipe recipe2 = new BlastingRecipe(
                    new ResourceLocation(id + "_blasting"),
                    group,
                    Ingredient.of(input),
                    new ItemStack(output, count),
                    xp,
                    time / 2
            );
            BCLRecipeManager.addRecipeAndCreateAdvancement(RecipeType.BLASTING, recipe2, false);
        }

        if (campfire) {
            CampfireCookingRecipe recipe2 = new CampfireCookingRecipe(
                    new ResourceLocation(id + "_campfire"),
                    group,
                    Ingredient.of(input),
                    new ItemStack(output, count),
                    xp,
                    time * 3
            );
            BCLRecipeManager.addRecipeAndCreateAdvancement(RecipeType.CAMPFIRE_COOKING, recipe2, false);
        }

        if (smoker) {
            SmokingRecipe recipe2 = new SmokingRecipe(
                    new ResourceLocation(id + "_smoker"),
                    group,
                    Ingredient.of(input),
                    new ItemStack(output, count),
                    xp,
                    time / 2
            );
            BCLRecipeManager.addRecipeAndCreateAdvancement(RecipeType.SMOKING, recipe2, false);
        }
    }
}
