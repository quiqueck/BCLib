package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseBookshelfBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Bookshelf extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Bookshelf() {
        super("bookshelf");
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BaseBookshelfBlock.Wood(parentMaterial.getBlock(WoodSlots.PLANKS));
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .setShape("###", "PPP", "###")
                .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                .addMaterial('P', Items.BOOK)
                .setGroup("bookshelf")
                .setCategory(RecipeCategory.BUILDING_BLOCKS)
                .build();
    }
}
