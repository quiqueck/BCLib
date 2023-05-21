package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseBarrelBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;
import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Barrel extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Barrel() {
        super("barrel");
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BaseBarrelBlock(parentMaterial.getBlock(WoodSlots.PLANKS));
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry.setBlockTags(BlockTags.MINEABLE_WITH_AXE, CommonBlockTags.BARREL, CommonBlockTags.WOODEN_BARREL)
             .setItemTags(CommonItemTags.BARREL, CommonItemTags.WOODEN_BARREL);
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .setShape("#S#", "# #", "#S#")
                .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                .addMaterial('S', parentMaterial.getBlock(WoodSlots.SLAB))
                .setGroup("barrel")
                .setCategory(RecipeCategory.DECORATIONS)
                .build();
    }
}
