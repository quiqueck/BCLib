package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.furniture.block.BaseChair;
import org.betterx.wover.recipe.api.BaseRecipeBuilder;
import org.betterx.wover.recipe.api.CraftingRecipeBuilder;
import org.betterx.wover.recipe.api.RecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Chair extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Chair() {
        super("chair");
    }

    public static void makeChairRecipe(RecipeOutput context, ResourceLocation id, Block chair, Block planks) {
        CraftingRecipeBuilder craftingRecipeBuilder1 = RecipeBuilder.crafting(id, chair);
        CraftingRecipeBuilder craftingRecipeBuilder = craftingRecipeBuilder1.shape("I ", "##", "II")
                                                                            .addMaterial('#', planks)
                                                                            .addMaterial('I', Items.STICK);
        BaseRecipeBuilder<CraftingRecipeBuilder> craftingRecipeBuilderBaseRecipeBuilder = craftingRecipeBuilder.group("chair");
        craftingRecipeBuilderBaseRecipeBuilder.category(RecipeCategory.DECORATIONS)
                                              .build(context);
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry.setBlockTags(BlockTags.MINEABLE_WITH_AXE);
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BaseChair.Wood(parentMaterial.getBlock(WoodSlots.PLANKS), parentMaterial.furnitureCloth());
    }

    @Override
    protected @Nullable void makeRecipe(RecipeOutput context, ComplexMaterial parentMaterial, ResourceLocation id) {
        Chair.makeChairRecipe(context, id, parentMaterial.getBlock(suffix), parentMaterial.getBlock(WoodSlots.SLAB));
    }
}
