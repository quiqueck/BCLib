package org.betterx.bclib.recipes;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class BCLRecipeBuilder {
    public static AlloyingRecipe.Builder alloying(ResourceLocation id, ItemLike output) {
        return AlloyingRecipe.Builder.create(id, output);
    }

    public static AnvilRecipe.Builder anvil(ResourceLocation id, ItemLike output) {
        return AnvilRecipe.create(id, output);
    }

    @Deprecated(forRemoval = true)
    public static CookingRecipeBuilder blasting(ResourceLocation id, ItemLike output) {
        return CookingRecipeBuilder.make(id, output).disableSmelter().enableBlastFurnace();
    }

    @Deprecated(forRemoval = true)
    public static CraftingRecipeBuilder crafting(ResourceLocation id, ItemLike output) {
        return CraftingRecipeBuilder.make(id, output);
    }


    private static CraftingRecipeBuilder copySmithingTemplateBase(
            ResourceLocation id,
            ItemLike filler,
            ItemLike output
    ) {
        return CraftingRecipeBuilder
                .make(id, output)
                .setOutputCount(2)
                .setCategory(RecipeCategory.MISC)
                .addMaterial('#', filler)
                .addMaterial('S', output)
                .setShape("#S#", "#C#", "###");
    }

    @Deprecated(forRemoval = true)
    public static CraftingRecipeBuilder copySmithingTemplate(
            ResourceLocation id,
            ItemLike output,
            TagKey<Item> tagKey
    ) {
        return copySmithingTemplateBase(id, Items.DIAMOND, output)
                .addMaterial('C', tagKey);
    }

    @Deprecated(forRemoval = true)
    public static CraftingRecipeBuilder copySmithingTemplate(
            ResourceLocation id,
            ItemLike output,
            ItemLike ingredient
    ) {
        return copySmithingTemplateBase(id, Items.DIAMOND, output)
                .addMaterial('C', ingredient);
    }

    @Deprecated(forRemoval = true)
    public static CraftingRecipeBuilder copyCheapSmithingTemplate(
            ResourceLocation id,
            ItemLike output,
            TagKey<Item> tagKey
    ) {
        return copyCheapSmithingTemplate(id, Items.STICK, output)
                .addMaterial('C', tagKey);
    }

    @Deprecated(forRemoval = true)
    public static CraftingRecipeBuilder copyCheapSmithingTemplate(
            ResourceLocation id,
            ItemLike output,
            ItemLike ingredient
    ) {
        return copySmithingTemplateBase(id, Items.STICK, output)
                .addMaterial('C', ingredient);
    }

    @Deprecated(forRemoval = true)
    public static CookingRecipeBuilder smelting(ResourceLocation id, ItemLike output) {
        return CookingRecipeBuilder.make(id, output);
    }

    @Deprecated(forRemoval = true)
    public static SmithingRecipeBuilder smithing(ResourceLocation id, ItemLike output) {
        return SmithingRecipeBuilder.make(id, output);
    }

    @Deprecated(forRemoval = true)
    public static StonecutterRecipeBuilder stonecutting(ResourceLocation id, ItemLike output) {
        return StonecutterRecipeBuilder.make(id, output);
    }
}
