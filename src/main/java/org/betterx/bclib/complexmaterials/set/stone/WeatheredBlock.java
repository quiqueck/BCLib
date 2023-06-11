package org.betterx.bclib.complexmaterials.set.stone;

import org.betterx.bclib.blocks.BaseBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.StoneComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WeatheredBlock extends SimpleMaterialSlot<StoneComplexMaterial> {
    public WeatheredBlock() {
        super("weathered");
    }

    @Override
    protected @NotNull Block createBlock(StoneComplexMaterial parentMaterial, BlockBehaviour.Properties settings) {
        return new BaseBlock.Stone(settings);
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder.crafting(
                                new ResourceLocation(id.getNamespace(), id.getPath() + "_from_moss"),
                                parentMaterial.getBlock(suffix)
                        )
                        .shapeless()
                        .addMaterial('#', parentMaterial.getBlock(StoneSlots.SOURCE))
                        .addMaterial('+', Blocks.MOSS_BLOCK)
                        .build();

        BCLRecipeBuilder.crafting(
                                new ResourceLocation(id.getNamespace(), id.getPath() + "_from_vine"),
                                parentMaterial.getBlock(suffix)
                        )
                        .shapeless()
                        .addMaterial('#', parentMaterial.getBlock(StoneSlots.SOURCE))
                        .addMaterial('+', Blocks.VINE)
                        .build();
    }
}
