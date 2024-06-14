package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseRotatedPillarBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.wover.recipe.api.BaseRecipeBuilder;
import org.betterx.wover.recipe.api.CraftingRecipeBuilder;
import org.betterx.wover.recipe.api.RecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StrippedLog extends SimpleMaterialSlot<WoodenComplexMaterial> {
    protected StrippedLog() {
        super("stripped_log");
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BaseRotatedPillarBlock.Wood(settings, parentMaterial.woodType.flammable);
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        if (parentMaterial.woodType.flammable) {
            entry
                    .setBlockTags(
                            BlockTags.LOGS,
                            BlockTags.LOGS_THAT_BURN,
                            parentMaterial.getBlockTag(WoodenComplexMaterial.TAG_LOGS)
                    )
                    .setItemTags(
                            ItemTags.LOGS,
                            ItemTags.LOGS_THAT_BURN,
                            parentMaterial.getItemTag(WoodenComplexMaterial.TAG_LOGS)
                    );
        } else {
            entry
                    .setBlockTags(
                            BlockTags.LOGS,
                            parentMaterial.getBlockTag(WoodenComplexMaterial.TAG_LOGS)
                    )
                    .setItemTags(
                            ItemTags.LOGS,
                            parentMaterial.getItemTag(WoodenComplexMaterial.TAG_LOGS)
                    );
        }
    }

    @Override
    protected @Nullable void makeRecipe(RecipeOutput context, ComplexMaterial material, ResourceLocation id) {
        CraftingRecipeBuilder craftingRecipeBuilder1 = RecipeBuilder
                .crafting(id, material.getBlock(suffix));
        CraftingRecipeBuilder craftingRecipeBuilder = craftingRecipeBuilder1.shape("##", "##")
                                                                            .addMaterial('#', material.getBlock(WoodSlots.STRIPPED_BARK));
        BaseRecipeBuilder<CraftingRecipeBuilder> craftingRecipeBuilderBaseRecipeBuilder = craftingRecipeBuilder.outputCount(3);
        craftingRecipeBuilderBaseRecipeBuilder.category(RecipeCategory.BUILDING_BLOCKS)
                                              .build(context);
    }
}
