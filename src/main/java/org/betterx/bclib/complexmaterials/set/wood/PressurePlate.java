package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.WoodenPressurePlateBlock;
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

public class PressurePlate extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public PressurePlate() {
        super("plate");
    }

    @Override
    protected @NotNull BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> getBlockSupplier(
            WoodenComplexMaterial parentMaterial
    ) {
        return (complexMaterial, settings) -> new WoodenPressurePlateBlock(
                complexMaterial.getBlock(WoodSlots.PLANKS),
                parentMaterial.woodType.setType()
        );
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry.setBlockTags(BlockTags.PRESSURE_PLATES, BlockTags.WOODEN_PRESSURE_PLATES)
             .setItemTags(ItemTags.WOODEN_PRESSURE_PLATES);
    }

    @Override
    protected @Nullable void getRecipeSupplier(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .setShape("##")
                .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                .setGroup("pressure_plate")
                .setCategory(RecipeCategory.REDSTONE)
                .build();
    }
}
