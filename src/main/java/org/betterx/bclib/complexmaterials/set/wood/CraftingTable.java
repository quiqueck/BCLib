package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.BaseCraftingTableBlock;
import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;
import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftingTable extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public CraftingTable() {
        super("crafting_table");
    }

    @Override
    protected @NotNull BiFunction<ComplexMaterial, BlockBehaviour.Properties, Block> getBlockSupplier(
            WoodenComplexMaterial parentMaterial
    ) {
        return (cmx, settings) -> new BaseCraftingTableBlock(cmx.getBlock(WoodSlots.PLANKS));
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry.setBlockTags(CommonBlockTags.WORKBENCHES)
             .setItemTags(CommonItemTags.WORKBENCHES);
    }

    @Override
    protected @Nullable void getRecipeSupplier(ComplexMaterial parentMaterial, ResourceLocation id) {
        BCLRecipeBuilder
                .crafting(id, parentMaterial.getBlock(suffix))
                .setShape("##", "##")
                .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                .setGroup("table")
                .setCategory(RecipeCategory.DECORATIONS)
                .build();
    }
}
