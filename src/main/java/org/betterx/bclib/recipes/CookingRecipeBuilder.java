package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class CookingRecipeBuilder extends AbstractUnlockableRecipeBuilder<CookingRecipeBuilder> {

    protected float xp;
    protected int cookingTime;

    boolean blasting, campfire, smoker, smelting;

    static CookingRecipeBuilder make(ResourceLocation id, ItemLike output) {
        return new CookingRecipeBuilder(id, output);
    }

    protected CookingRecipeBuilder(ResourceLocation id, ItemLike output) {
        super(id, output);
        this.xp = 0;
        this.cookingTime = 200;
        this.smelting = true;
    }


    /**
     * Use {@link #setPrimaryInputAndUnlock(ItemLike...)} instead
     *
     * @deprecated Use {@link #setPrimaryInputAndUnlock(ItemLike...)} instead
     */
    @Deprecated(forRemoval = true)
    public CookingRecipeBuilder setInput(ItemLike in) {
        return this.setPrimaryInputAndUnlock(in);
    }

    /**
     * Use {@link #setPrimaryInputAndUnlock(ItemLike...)} instead
     *
     * @param in
     * @return
     * @deprecated Use {@link #setPrimaryInputAndUnlock(ItemLike...)} instead
     */
    @Deprecated(forRemoval = true)
    public CookingRecipeBuilder setInput(TagKey<Item> in) {
        return this.setPrimaryInputAndUnlock(in);
    }

    public CookingRecipeBuilder setExperience(float xp) {
        this.xp = xp;
        return this;
    }

    public CookingRecipeBuilder setCookingTime(int time) {
        this.cookingTime = time;
        return this;
    }

    @Override
    protected boolean checkRecipe() {
        if (smelting == false && blasting == false && campfire == false && smoker == false) {
            BCLib.LOGGER.warning(
                    "No target (smelting, blasting, campfire or somer) for cooking recipe was selected. Recipe {} will be ignored!",
                    id
            );
            return false;
        }

        if (cookingTime < 0) {
            BCLib.LOGGER.warning(
                    "cooking time must be positive. Recipe {} will be ignored!",
                    id
            );
            return false;
        }
        return super.checkRecipe();
    }

    public CookingRecipeBuilder enableSmelter() {
        this.smelting = true;
        return this;
    }

    public CookingRecipeBuilder disableSmelter() {
        this.smelting = false;
        return this;
    }

    public CookingRecipeBuilder enableBlastFurnace() {
        this.blasting = true;
        return this;
    }

    public CookingRecipeBuilder disableBlastFurnace() {
        this.blasting = false;
        return this;
    }

    public CookingRecipeBuilder enableCampfire() {
        this.campfire = true;
        return this;
    }

    public CookingRecipeBuilder disableCampfire() {
        this.campfire = false;
        return this;
    }

    public CookingRecipeBuilder enableSmoker() {
        this.smoker = true;
        return this;
    }

    public CookingRecipeBuilder disableSmoker() {
        this.smoker = false;
        return this;
    }

    public void build(boolean blasting, boolean campfire, boolean smoker) {
        this.enableSmelter();
        this.blasting = blasting;
        this.campfire = campfire;
        this.smoker = smoker;

        build();
    }

    public void buildWithBlasting() {
        build(true, false, false);
    }

    public void buildFoodlike() {
        build(false, true, true);
    }

    private void buildRecipe(Consumer<FinishedRecipe> cc, SimpleCookingRecipeBuilder builder, String postfix) {
        ResourceLocation loc = new ResourceLocation(id.getNamespace(), id.getPath() + "_" + postfix);
        for (var item : unlocks.entrySet()) {
            builder.unlockedBy(item.getKey(), item.getValue());
        }
        builder.save(cc, loc);
    }

    @Override
    protected void buildRecipe(Consumer<FinishedRecipe> cc) {
        if (smelting) {
            buildRecipe(
                    cc,
                    SimpleCookingRecipeBuilder.smelting(
                            primaryInput,
                            category,
                            output.getItem(),
                            xp,
                            cookingTime
                    ),
                    "smelting"
            );
        }

        if (blasting) {
            buildRecipe(
                    cc,
                    SimpleCookingRecipeBuilder.blasting(
                            primaryInput,
                            category,
                            output.getItem(),
                            xp,
                            cookingTime / 2
                    ),
                    "blasting"
            );
        }

        if (campfire) {
            buildRecipe(
                    cc,
                    SimpleCookingRecipeBuilder.campfireCooking(
                            primaryInput,
                            category,
                            output.getItem(),
                            xp,
                            cookingTime * 3
                    ),
                    "campfire"
            );
        }

        if (smoker) {
            buildRecipe(
                    cc,
                    SimpleCookingRecipeBuilder.campfireCooking(
                            primaryInput,
                            category,
                            output.getItem(),
                            xp,
                            cookingTime / 2
                    ),
                    "smoker"
            );
        }
    }
}
