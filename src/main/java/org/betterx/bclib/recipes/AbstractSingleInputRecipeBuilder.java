package org.betterx.bclib.recipes;

import org.betterx.bclib.util.ItemUtil;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import com.google.gson.JsonObject;

import java.util.function.Consumer;
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
    protected T unlocks(String name, CriterionTriggerInstance trigger) {
        this.advancement.addCriterion(name, trigger);
        return (T) this;
    }


    @Override
    protected void buildRecipe(Consumer<FinishedRecipe> cc) {
        setupAdvancementForResult();
        cc.accept(new AbstractSingleInputRecipeBuilder<T, R>.Result());
    }

    protected abstract RecipeSerializer<R> getSerializer();

    protected void serializeRecipeData(JsonObject root) {
        root.add("input", ItemUtil.toJsonIngredientWithNBT(primaryInput));

        if (group != null && !group.isEmpty()) {
            root.addProperty("group", group);
        }

        root.add("result", ItemUtil.toJsonRecipeWithNBT(output));
    }

    protected void setupAdvancementForResult() {
        advancement.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
                   .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                   .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(id))
                   .requirements(RequirementsStrategy.OR);
    }

    protected ResourceLocation createAdvancementId() {
        return id.withPrefix("recipes/" + category.getFolderName() + "/");
    }

    public class Result implements FinishedRecipe {
        private final ResourceLocation advancementId;

        protected Result() {
            this.advancementId = createAdvancementId();
        }

        @Override
        public ResourceLocation getId() {
            return AbstractSingleInputRecipeBuilder.this.getId();
        }

        @Override
        public RecipeSerializer<R> getType() {
            return getSerializer();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return advancementId;
        }

        @Override
        public void serializeRecipeData(JsonObject root) {
            AbstractSingleInputRecipeBuilder.this.serializeRecipeData(root);
        }
    }
}
