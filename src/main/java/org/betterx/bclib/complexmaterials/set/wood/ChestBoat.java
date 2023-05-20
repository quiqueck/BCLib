package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.ItemEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.recipes.BCLRecipeBuilder;
import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChestBoat extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public ChestBoat() {
        super("chest_boat");
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
        return new ItemEntry(
                suffix,
                (cmx, settings) -> parentMaterial.getBoatType().createItem(true)
        ).setItemTags(new TagKey[]{ItemTags.CHEST_BOATS});
    }

    @Override
    protected @Nullable void makeRecipe(ComplexMaterial parentMaterial, ResourceLocation id) {
        makeChestBoatRecipe(id, parentMaterial.getItem(WoodSlots.BOAT), parentMaterial.getItem(WoodSlots.CHEST_BOAT));
    }

    @Override
    public void onInit(WoodenComplexMaterial parentMaterial) {
        parentMaterial.initBoatType();
    }

    public static void makeChestBoatRecipe(ResourceLocation id, Item boat, Item chestBoat) {
        BCLRecipeBuilder
                .crafting(id, chestBoat)
                .shapeless()
                .addMaterial('C', CommonItemTags.CHEST)
                .addMaterial('#', boat)
                .setGroup("chest_boat")
                .setCategory(RecipeCategory.TRANSPORTATION)
                .build();
    }
}
