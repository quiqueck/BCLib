package org.betterx.bclib.complexmaterials.set.stone;

import org.betterx.bclib.blocks.BaseWallBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.StoneComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.set.common.AbstractWall;
import org.betterx.wover.recipe.api.RecipeBuilder;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Wall extends AbstractWall<StoneComplexMaterial> {
    private final MaterialSlot<StoneComplexMaterial> base;

    public Wall() {
        super();
        this.base = StoneSlots.SOURCE;
    }

    public Wall(MaterialSlot<StoneComplexMaterial> base) {
        super(base.suffix);
        this.base = base;
    }

    @Override
    protected @NotNull Block createBlock(StoneComplexMaterial parentMaterial, BlockBehaviour.Properties settings) {
        return new BaseWallBlock.Stone(parentMaterial.getBlock(getSourceBlockSlot()));
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
                .outputCount(1)
                .group("wall")
                .build(context);
    }

    @Override
    protected @Nullable MaterialSlot<StoneComplexMaterial> getSourceBlockSlot() {
        return base;
    }
}
