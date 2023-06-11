package org.betterx.bclib.complexmaterials.set.stone;

import org.betterx.bclib.blocks.BaseBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.StoneComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrackedBlock extends SimpleMaterialSlot<StoneComplexMaterial> {
    public CrackedBlock() {
        super("cracked");
    }

    @Override
    protected @NotNull Block createBlock(StoneComplexMaterial parentMaterial, BlockBehaviour.Properties settings) {
        return new BaseBlock.Stone(settings);
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder.smelting(id, parentMaterial.getBlock(suffix))
                        .setPrimaryInputAndUnlock(parentMaterial.getBlock(StoneSlots.SOURCE))
                        .build(false, false, false);
    }
}
