package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.ItemEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Boat extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Boat() {
        super("boat");
    }

    @Override
    public void addBlockEntry(WoodenComplexMaterial parentMaterial, Consumer<BlockEntry> adder) {
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        //this should never get called
        return null;
    }

    @Override
    protected @Nullable ItemEntry getItemEntry(WoodenComplexMaterial parentMaterial) {
        return new ItemEntry(suffix, (cmx, settings) -> parentMaterial.getBoatType().createItem(false));
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        makeBoatRecipe(id, parentMaterial.getBlock(WoodSlots.PLANKS), parentMaterial.getItem(suffix));
    }

    @Override
    public void onInit(WoodenComplexMaterial parentMaterial) {
        parentMaterial.initBoatType();
    }

    public static void makeBoatRecipe(ResourceLocation id, Block planks, Item boat) {
        BCLRecipeBuilder
                .crafting(id, boat)
                .setShape("# #", "###")
                .addMaterial('#', planks)
                .setGroup("boat")
                .setCategory(RecipeCategory.TRANSPORTATION)
                .build();
    }
}
