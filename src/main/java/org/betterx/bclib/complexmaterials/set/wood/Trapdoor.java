package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseTrapdoorBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Trapdoor extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Trapdoor() {
        super("trapdoor");
    }

    @Override
    protected @NotNull BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> getBlockSupplier(
            WoodenComplexMaterial parentMaterial
    ) {
        return (complexMaterial, settings) -> new BaseTrapdoorBlock(
                complexMaterial.getBlock(WoodSlots.PLANKS),
                parentMaterial.woodType.setType()
        );
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry.setBlockTags(BlockTags.TRAPDOORS, BlockTags.WOODEN_TRAPDOORS)
             .setItemTags(ItemTags.TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
    }

    @Override
    protected @Nullable void getRecipeSupplier(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder.crafting(id, parentMaterial.getBlock(suffix))
                        .setOutputCount(2)
                        .setShape("###", "###")
                        .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                        .setGroup("trapdoor")
                        .setCategory(RecipeCategory.REDSTONE)
                        .build();
    }
}
