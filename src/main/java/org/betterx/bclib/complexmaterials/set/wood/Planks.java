package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BasePlanks;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.wover.recipe.api.RecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
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
    protected @Nullable void makeRecipe(RecipeOutput context, ComplexMaterial parentMaterial, ResourceLocation id) {
        RecipeBuilder.crafting(id, parentMaterial.getBlock(suffix))
                     .outputCount(4)
                     .shapeless()
                     .addMaterial(
                             '#',
                             parentMaterial.getBlock(WoodSlots.LOG),
                             parentMaterial.getBlock(WoodSlots.BARK),
                             parentMaterial.getBlock(WoodSlots.STRIPPED_LOG),
                             parentMaterial.getBlock(WoodSlots.STRIPPED_BARK)
                     )
                     .group("planks")
                     .category(RecipeCategory.BUILDING_BLOCKS)
                     .build(context);
    }
}
