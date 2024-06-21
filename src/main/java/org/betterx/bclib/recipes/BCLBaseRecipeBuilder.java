package org.betterx.bclib.recipes;

import org.betterx.bclib.util.BCLDataComponents;
import org.betterx.wover.recipe.api.BaseRecipeBuilder;
import org.betterx.wover.recipe.impl.BaseRecipeBuilderImpl;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public abstract class BCLBaseRecipeBuilder<I extends BaseRecipeBuilder<I>, R extends Recipe<? extends RecipeInput>> extends BaseRecipeBuilderImpl<I> {
    public interface RecipeOutputConsumer extends Consumer<CompoundTag> {
    }

    protected final Advancement.Builder advancement;
    protected Ingredient primaryInput;
    protected Ingredient secondaryInput;
    protected RecipeOutputConsumer outputTagConsumer;

    protected BCLBaseRecipeBuilder(
            @NotNull ResourceLocation id,
            @NotNull ItemLike output
    ) {
        this(id, new ItemStack(output, 1));
    }

    protected BCLBaseRecipeBuilder(@NotNull ResourceLocation id, @NotNull ItemStack output) {
        super(id, output);
        this.advancement = Advancement.Builder.advancement();
    }

    @Override
    protected void validate() {
        super.validate();
        if (primaryInput == null || primaryInput.isEmpty()) {
            throwIllegalStateException(
                    "Primary input for Recipe can't be 'null', recipe {} will be ignored!"
            );
        }
        if (secondaryInput == null) {
            throwIllegalStateException(
                    "Secondary input for Recipe can't be 'null', recipe {} will be ignored!"
            );
        }
    }

    @Override
    public final void build(RecipeOutput ctx) {
        validate();

        setupAdvancementForResult();
        final AdvancementHolder advancementHolder = advancement.build(createAdvancementId());

        if (this.outputTagConsumer != null)
            CustomData.update(BCLDataComponents.ANVIL_ENTITY_DATA, this.output, this.outputTagConsumer);

        final R recipe = createRecipe(id);
        ctx.accept(id, recipe, advancementHolder);
    }

    protected abstract R createRecipe(ResourceLocation id);

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

    public I setPrimaryInput(ItemLike... inputs) {
        this.primaryInput = Ingredient.of(inputs);
        return (I) this;
    }

    public I setPrimaryInput(TagKey<Item> input) {
        this.primaryInput = Ingredient.of(input);
        return (I) this;
    }

    public I setPrimaryInputAndUnlock(TagKey<Item> input) {
        this.setPrimaryInput(input);
        this.unlockedBy(input);
        return (I) this;
    }

    public I setPrimaryInputAndUnlock(ItemLike... inputs) {
        setPrimaryInput(inputs);
        for (ItemLike item : inputs) unlockedBy(item);

        return (I) this;
    }

    public I setSecondaryInput(ItemLike... inputs) {
        this.secondaryInput = Ingredient.of(inputs);
        return (I) this;
    }

    public I setSecondaryInput(TagKey<Item> input) {
        this.secondaryInput = Ingredient.of(input);
        return (I) this;
    }

    public I setSecondaryInputAndUnlock(TagKey<Item> input) {
        setSecondaryInput(input);
        this.unlockedBy(input);
        return (I) this;
    }

    public I setSecondaryInputAndUnlock(ItemLike... inputs) {
        setSecondaryInput(inputs);
        for (ItemLike item : inputs) unlockedBy(item);

        return (I) this;
    }

    public I setOutputTag(CompoundTag tag) {
        this.outputTagConsumer = (itemTag) -> {
            for (String k : tag.getAllKeys()) {
                itemTag.put(k, tag.get(k));
            }
        };
        return (I) this;
    }

    public I setOutputTag(RecipeOutputConsumer consumer) {
        this.outputTagConsumer = consumer;
        return (I) this;
    }

}
