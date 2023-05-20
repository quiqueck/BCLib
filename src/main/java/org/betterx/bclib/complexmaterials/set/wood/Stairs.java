package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseStairsBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Stairs extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Stairs() {
        super("stairs");
    }

    @Override
    protected @NotNull BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> getBlockSupplier(
            WoodenComplexMaterial parentMaterial
    ) {
        return (complexMaterial, settings) -> new BaseStairsBlock(complexMaterial.getBlock(WoodSlots.PLANKS), false);
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry.setBlockTags(BlockTags.WOODEN_STAIRS, BlockTags.STAIRS)
             .setItemTags(ItemTags.WOODEN_STAIRS, ItemTags.STAIRS);
    }

    @Override
    protected @Nullable void getRecipeSupplier(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .setOutputCount(4)
                .setShape("#  ", "## ", "###")
                .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                .setGroup("stairs")
                .setCategory(RecipeCategory.BUILDING_BLOCKS)
                .build();
    }
}
