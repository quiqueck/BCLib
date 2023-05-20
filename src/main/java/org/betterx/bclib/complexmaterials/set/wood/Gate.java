package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseGateBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Gate extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Gate() {
        super("gate");
    }

    @Override
    protected @NotNull BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> getBlockSupplier(
            WoodenComplexMaterial parentMaterial
    ) {
        return (complexMaterial, settings) -> new BaseGateBlock(
                complexMaterial.getBlock(WoodSlots.PLANKS),
                parentMaterial.woodType.type()
        );
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry.setBlockTags(BlockTags.FENCE_GATES);
    }

    @Override
    protected @Nullable void getRecipeSupplier(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder.crafting(id, parentMaterial.getBlock(suffix))
                        .setShape("I#I", "I#I")
                        .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                        .addMaterial('I', Items.STICK)
                        .setGroup("gate")
                        .setCategory(RecipeCategory.REDSTONE)
                        .build();
    }
}
