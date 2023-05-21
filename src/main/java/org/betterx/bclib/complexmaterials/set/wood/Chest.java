package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseChestBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;
import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Chest extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Chest() {
        super("chest");
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BaseChestBlock(parentMaterial.getBlock(WoodSlots.PLANKS));
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry.setBlockTags(BlockTags.MINEABLE_WITH_AXE, CommonBlockTags.CHEST, CommonBlockTags.WOODEN_CHEST)
             .setItemTags(CommonItemTags.CHEST, CommonItemTags.WOODEN_CHEST);
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .setShape("###", "# #", "###")
                .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                .setGroup("chest")
                .setCategory(RecipeCategory.DECORATIONS)
                .build();
    }
}
