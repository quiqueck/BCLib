package org.betterx.bclib.complexmaterials.set.stone;

import org.betterx.bclib.blocks.BaseSlabBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.StoneComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.set.common.AbstractSlab;
import org.betterx.wover.recipe.api.RecipeBuilder;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Slab extends AbstractSlab<StoneComplexMaterial> {
    private final MaterialSlot<StoneComplexMaterial> base;

    public Slab() {
        super();
        this.base = StoneSlots.SOURCE;
    }

    public Slab(MaterialSlot<StoneComplexMaterial> base) {
        super(base.suffix);
        this.base = base;
    }

    @Override
    protected @NotNull Block createBlock(
            StoneComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BaseSlabBlock.Stone(parentMaterial.getBlock(getSourceBlockSlot()));
    }

    @Override
    protected @Nullable void makeRecipe(RecipeOutput context, ComplexMaterial parentMaterial, ResourceLocation id) {
        super.makeRecipe(context, parentMaterial, id);

        RecipeBuilder
                .stonecutting(
                        ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "stonecutter_" + id.getPath()),
                        parentMaterial.getBlock(suffix)
                )
                .input(parentMaterial.getBlock(getSourceBlockSlot()))
                .outputCount(2)
                .group("slab")
                .build(context);
    }

    @Nullable
    @Override
    protected MaterialSlot<StoneComplexMaterial> getSourceBlockSlot() {
        return base;
    }
}
