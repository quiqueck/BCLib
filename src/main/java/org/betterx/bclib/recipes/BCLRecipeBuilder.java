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

    public static CookingRecipeBuilder blasting(ResourceLocation id, ItemLike output) {
        return CookingRecipeBuilder.make(id, output).disableSmelter().enableBlastFurnace();
    }

    public static CraftingRecipeBuilder crafting(ResourceLocation id, ItemLike output) {
        return CraftingRecipeBuilder.make(id, output);
    }

    private static CraftingRecipeBuilder copySmithingTemplateBase(
            ResourceLocation id,
            ItemLike output
    ) {
        return CraftingRecipeBuilder
                .make(id, output)
                .setOutputCount(2)
                .setCategory(RecipeCategory.MISC)
                .addMaterial('#', Items.DIAMOND)
                .addMaterial('S', output)
                .setShape("#S#", "#C#", "###");
    }

    public static CraftingRecipeBuilder copySmithingTemplate(
            ResourceLocation id,
            ItemLike output,
            TagKey<Item> tagKey
    ) {
        return copySmithingTemplateBase(id, output)
                .addMaterial('C', tagKey);
    }

    public static CraftingRecipeBuilder copySmithingTemplate(
            ResourceLocation id,
            ItemLike output,
            ItemLike ingredient
    ) {
        return copySmithingTemplateBase(id, output)
                .addMaterial('C', ingredient);
    }

    public static CookingRecipeBuilder smelting(ResourceLocation id, ItemLike output) {
        return CookingRecipeBuilder.make(id, output);
    }

    public static SmithingRecipeBuilder smithing(ResourceLocation id, ItemLike output) {
        return SmithingRecipeBuilder.make(id, output);
    }

    public static StonecutterRecipeBuilder stonecutting(ResourceLocation id, ItemLike output) {
        return StonecutterRecipeBuilder.make(id, output);
    }
}
