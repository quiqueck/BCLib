package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseStripableLogBlock;
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

public class Log extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Log() {
        super("log");
    }

    @Override
    protected @NotNull BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> getBlockSupplier(
            WoodenComplexMaterial parentMaterial
    ) {
        return (complexMaterial, settings) -> new BaseStripableLogBlock(
                parentMaterial.woodColor,
                complexMaterial.getBlock(WoodSlots.STRIPPED_LOG)
        );
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry
                .setBlockTags(
                        BlockTags.LOGS,
                        BlockTags.LOGS_THAT_BURN,
                        parentMaterial.getBlockTag(WoodenComplexMaterial.TAG_LOGS)
                )
                .setItemTags(
                        ItemTags.LOGS,
                        ItemTags.LOGS_THAT_BURN,
                        parentMaterial.getItemTag(WoodenComplexMaterial.TAG_LOGS)
                );
    }

    @Override
    protected @Nullable void getRecipeSupplier(ComplexMaterial material, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, material.getBlock(suffix))
                .setShape("##", "##")
                .addMaterial('#', material.getBlock(WoodSlots.BARK))
                .setOutputCount(3)
                .setCategory(RecipeCategory.BUILDING_BLOCKS)
                .build();
    }
}
