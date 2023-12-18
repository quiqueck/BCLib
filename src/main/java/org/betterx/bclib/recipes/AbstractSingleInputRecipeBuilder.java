package org.betterx.bclib.recipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractSingleInputRecipeBuilder<T extends AbstractSingleInputRecipeBuilder, R extends Recipe<? extends Container>> extends AbstractSimpleRecipeBuilder<T> {
    protected final Advancement.Builder advancement;


    protected AbstractSingleInputRecipeBuilder(ResourceLocation id, ItemLike output) {
        this(id, new ItemStack(output, 1));
    }

    protected AbstractSingleInputRecipeBuilder(ResourceLocation id, ItemStack output) {
        super(id, output);
        this.advancement = Advancement.Builder.advancement();
    }

    @Override
    public T unlockedBy(ItemLike item) {
        return super.unlockedBy(item);
    }

    @Override
    public T unlockedBy(TagKey<Item> tag) {
        return super.unlockedBy(tag);
    }

    @Override
    protected T unlocks(String name, Criterion<?> trigger) {
        this.advancement.addCriterion(name, trigger);
        return (T) this;
    }


    @Override
    protected void buildRecipe(RecipeOutput cc) {
        setupAdvancementForResult();
        final AdvancementHolder advancementHolder = advancement.build(getId());
        final R recipe = createRecipe(getId());
        cc.accept(getId(), recipe, advancementHolder);
    }

    protected abstract R createRecipe(ResourceLocation id);

    protected abstract RecipeSerializer<R> getSerializer();

    protected void setupAdvancementForResult() {
        advancement
                .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)//automatically at root level
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
    }

    protected ResourceLocation createAdvancementId() {
        return id.withPrefix("recipes/" + category.getFolderName() + "/");
    }

    public class Result implements RecipeOutput {
        @Override
        public void accept(
                ResourceLocation resourceLocation,
                Recipe<?> recipe,
                @Nullable AdvancementHolder advancementHolder
        ) {

        }

        @Override
        public Advancement.Builder advancement() {
            return null;
        }
    }
}
