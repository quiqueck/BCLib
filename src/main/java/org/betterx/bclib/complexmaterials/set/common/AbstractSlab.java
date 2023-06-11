package org.betterx.bclib.complexmaterials.set.common;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractSlab<M extends ComplexMaterial> extends SimpleMaterialSlot<M> {
    public AbstractSlab() {
        super("slab");
    }

    protected AbstractSlab(String prefix) {
        super(prefix + "_slab");
    }


    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .setOutputCount(6)
                .setShape("###")
                .addMaterial('#', parentMaterial.getBlock(getSourceBlockSlot()))
                .setGroup("slab")
                .setCategory(RecipeCategory.BUILDING_BLOCKS)
                .build();
    }

    @Nullable
    protected abstract MaterialSlot<M> getSourceBlockSlot();
}
