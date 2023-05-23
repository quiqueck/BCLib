package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BasePlanks;
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

public class Planks extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Planks() {
        super("planks");
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BasePlanks.Wood(settings);
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder.crafting(id, parentMaterial.getBlock(suffix))
                        .setOutputCount(4)
                        .shapeless()
                        .addMaterial(
                                '#',
                                parentMaterial.getBlock(WoodSlots.LOG),
                                parentMaterial.getBlock(WoodSlots.BARK),
                                parentMaterial.getBlock(WoodSlots.STRIPPED_LOG),
                                parentMaterial.getBlock(WoodSlots.STRIPPED_BARK)
                        )
                        .setGroup("planks")
                        .setCategory(RecipeCategory.BUILDING_BLOCKS)
                        .build();
    }
}
