package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class SmithingRecipeBuilder extends AbstractUnlockableRecipeBuilder<SmithingRecipeBuilder> {
    protected Ingredient addon;

    protected SmithingRecipeBuilder(
            ResourceLocation id,
            ItemLike output
    ) {
        super(id, output);
    }

    static SmithingRecipeBuilder make(ResourceLocation id, ItemLike output) {
        return new SmithingRecipeBuilder(id, output);
    }

    /**
     * @param in
     * @return
     * @deprecated Use {@link #setPrimaryInputAndUnlock(ItemLike...)} instead
     */
    @Deprecated(forRemoval = true)
    public SmithingRecipeBuilder setBase(ItemLike in) {
        return super.setPrimaryInputAndUnlock(in);
    }

    /**
     * @param in
     * @return
     * @deprecated use {@link #setPrimaryInputAndUnlock(TagKey)} instead
     */
    @Deprecated(forRemoval = true)
    public SmithingRecipeBuilder setBase(TagKey<Item> in) {
        return super.setPrimaryInputAndUnlock(in);
    }

    public SmithingRecipeBuilder setAddition(ItemLike item) {
        this.addon = Ingredient.of(item);
        return this;
    }


    @Override
    protected boolean checkRecipe() {
        if (addon == null || addon.isEmpty()) {
            BCLib.LOGGER.warning(
                    "Addon for Recipe can't be 'null', recipe {} will be ignored!",
                    id
            );
            return false;
        }
        return super.checkRecipe();
    }

    @Override
    protected void buildRecipe(Consumer<FinishedRecipe> cc) {
        final SmithingTransformRecipeBuilder builder = SmithingTransformRecipeBuilder.smithing(
                Ingredient.EMPTY, primaryInput, addon, category, output.getItem()
        );
        for (var item : unlocks.entrySet()) {
            builder.unlocks(item.getKey(), item.getValue());
        }
        builder.save(cc, id);
    }
}
