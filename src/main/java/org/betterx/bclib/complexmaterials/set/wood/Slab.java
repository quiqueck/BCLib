package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseSlabBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Slab extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Slab() {
        super("slab");
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BaseSlabBlock.Wood(parentMaterial.getBlock(WoodSlots.PLANKS), !parentMaterial.woodType.flammable);
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .setOutputCount(6)
                .setShape("###")
                .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                .setGroup("slab")
                .setCategory(RecipeCategory.BUILDING_BLOCKS)
                .build();
    }
}
