package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseWallBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.set.common.AbstractWall;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Wall extends AbstractWall<WoodenComplexMaterial> {
    @Override
    protected @NotNull Block createBlock(WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings) {
        return new BaseWallBlock.Wood(parentMaterial.getBlock(getSourceBlockSlot()));
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder.crafting(id, parentMaterial.getBlock(suffix))
                        .setOutputCount(6)
                        .setShape("* *", "|||")
                        .addMaterial('*', parentMaterial.getBlock(WoodSlots.PLANKS))
                        .addMaterial('|', parentMaterial.getBlock(WoodSlots.FENCE))
                        .setGroup("wall")
                        .build();
    }

    @Override
    protected @Nullable MaterialSlot<WoodenComplexMaterial> getSourceBlockSlot() {
        return WoodSlots.PLANKS;
    }
}
