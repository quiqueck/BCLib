package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseBarkBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StrippedBark extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public StrippedBark() {
        super("stripped_bark");
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BaseBarkBlock.Wood(settings, parentMaterial.woodType.flammable);
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry
                .setBlockTags(
                        parentMaterial.getBlockTag(WoodenComplexMaterial.TAG_LOGS)
                )
                .setItemTags(
                        parentMaterial.getItemTag(WoodenComplexMaterial.TAG_LOGS)
                );
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial material, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, material.getBlock(suffix))
                .setShape("##", "##")
                .addMaterial('#', material.getBlock(WoodSlots.STRIPPED_LOG))
                .setOutputCount(3)
                .setCategory(RecipeCategory.BUILDING_BLOCKS)
                .build();
    }
}
