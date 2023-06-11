package org.betterx.bclib.complexmaterials.set.common;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractStairs<M extends ComplexMaterial> extends SimpleMaterialSlot<M> {
    public AbstractStairs() {
        super("stairs");
    }

    protected AbstractStairs(String prefix) {
        super(prefix + "_stairs");
    }


    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .setOutputCount(4)
                .setShape("#  ", "## ", "###")
                .addMaterial('#', parentMaterial.getBlock(getSourceBlockSlot()))
                .setGroup("stairs")
                .setCategory(RecipeCategory.BUILDING_BLOCKS)
                .build();
    }

    @Nullable
    protected abstract MaterialSlot<M> getSourceBlockSlot();
}
