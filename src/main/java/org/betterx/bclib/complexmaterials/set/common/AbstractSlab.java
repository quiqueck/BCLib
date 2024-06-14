package org.betterx.bclib.complexmaterials.set.common;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.wover.recipe.api.RecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
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
    protected @Nullable void makeRecipe(RecipeOutput context, ComplexMaterial parentMaterial, ResourceLocation id) {
        RecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .outputCount(6)
                .shape("###")
                .addMaterial('#', parentMaterial.getBlock(getSourceBlockSlot()))
                .group("slab")
                .category(RecipeCategory.BUILDING_BLOCKS)
                .build(context);
    }

    @Nullable
    protected abstract MaterialSlot<M> getSourceBlockSlot();
}
