package org.betterx.bclib.recipes;

import org.betterx.bclib.util.BCLDataComponents;
import org.betterx.wover.recipe.api.BaseRecipeBuilder;
import org.betterx.wover.recipe.impl.BaseRecipeBuilderImpl;
import org.betterx.wover.recipe.impl.CraftingRecipeBuilderImpl;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
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
    protected CraftingRecipeBuilderImpl.IngredientFactory primaryInput;
    protected CraftingRecipeBuilderImpl.IngredientFactory secondaryInput;
    protected RecipeOutputConsumer outputTagConsumer;

    private final boolean dualInput;

    protected BCLBaseRecipeBuilder(
            @NotNull ResourceLocation id,
            @NotNull ItemLike output,
            boolean dualInput
    ) {
        this(id, new ItemStack(output, 1), dualInput);
    }

    protected BCLBaseRecipeBuilder(@NotNull ResourceLocation id, @NotNull ItemStack output, boolean dualInput) {
        super(id, output);
        this.advancement = Advancement.Builder.advancement();
        this.dualInput = dualInput;
        this.group("");
    }

    @Override
    protected void validate() {
        super.validate();
        if (primaryInput == null) {
            throwIllegalStateException(
                    "Primary input for Recipe can't be 'null', recipe {} will be ignored!"
            );
        }
        if (secondaryInput == null && this.dualInput) {
            throwIllegalStateException(
                    "Secondary input for Recipe can't be 'null', recipe {} will be ignored!"
            );
        }
    }

    @Override
    public void build(org.betterx.wover.recipe.api.RecipeBuilder.Context ctx) {
        validate();

        setupAdvancementForResult();
        final AdvancementHolder advancementHolder = advancement.build(createAdvancementId());

        if (this.outputTagConsumer != null)
            CustomData.update(BCLDataComponents.ANVIL_ENTITY_DATA, this.output, this.outputTagConsumer);

        final R recipe = createRecipe(ctx);
        ctx.recipeOutput().accept(key, recipe, advancementHolder);
    }

    protected abstract R createRecipe(org.betterx.wover.recipe.api.RecipeBuilder.Context ctx);

    @SuppressWarnings("removal")
    protected void setupAdvancementForResult() {
        advancement
                .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)//automatically at root level
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(key))
                .requirements(AdvancementRequirements.Strategy.OR);
    }

    protected ResourceLocation createAdvancementId() {
        return key.withPrefix("recipes/" + category.getFolderName() + "/");
    }

    public I setPrimaryInput(ItemLike... inputs) {
        this.primaryInput = provider -> Ingredient.of(inputs);
        return (I) this;
    }

    public I setPrimaryInput(TagKey<Item> input) {
        this.primaryInput = provider -> provider.tag(input);
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
        this.secondaryInput = provider -> Ingredient.of(inputs);
        return (I) this;
    }

    public I setSecondaryInput(TagKey<Item> input) {
        this.secondaryInput = provider -> provider.tag(input);
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
            for (String k : tag.keySet()) {
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
