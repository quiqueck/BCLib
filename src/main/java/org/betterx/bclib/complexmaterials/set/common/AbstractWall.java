package org.betterx.bclib.complexmaterials.set.common;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.complexmaterials.set.stone.StoneSlots;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractWall<M extends ComplexMaterial> extends SimpleMaterialSlot<M> {
    public AbstractWall() {
        super("wall");
    }

    protected AbstractWall(@NotNull String postfix) {
        super(postfix + "_wall");
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder.crafting(id, parentMaterial.getBlock(suffix))
                        .setOutputCount(6)
                        .setShape("###", "###")
                        .addMaterial('#', parentMaterial.getBlock(StoneSlots.SOURCE))
                        .setGroup("wall")
                        .build();
    }

    @Nullable
    protected abstract MaterialSlot<M> getSourceBlockSlot();
}
