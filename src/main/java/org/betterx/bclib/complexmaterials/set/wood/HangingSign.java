package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.signs.BaseHangingSignBlock;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.entry.RecipeEntry;
import org.betterx.bclib.recipes.BCLRecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public class HangingSign extends MaterialSlot<WoodenComplexMaterial> {
    @NotNull
    public static final String WALL_SUFFFIX = "wall_hanging_sign";

    public HangingSign() {
        super("hanging_sign");
    }

    @Override
    public void addBlockEntry(WoodenComplexMaterial parentMaterial, Consumer<BlockEntry> adder) {
        var signEntry = new BlockEntry(
                suffix,
                (complexMaterial, settings) -> new BaseHangingSignBlock(parentMaterial.woodType)
        ).setBlockTags(BlockTags.CEILING_HANGING_SIGNS)
         .setItemTags(ItemTags.HANGING_SIGNS);

        var wallSignEntry = new BlockEntry(
                WALL_SUFFFIX,
                false,
                (complexMaterial, settings) -> {
                    if (complexMaterial.getBlock(suffix) instanceof BaseHangingSignBlock sign) {
                        return sign.wallSign;
                    }
                    return null;
                }
        ).setBlockTags(BlockTags.WALL_HANGING_SIGNS);
        adder.accept(signEntry);
        adder.accept(wallSignEntry);
    }

    @Override
    public void addRecipeEntry(
            WoodenComplexMaterial parentMaterial,
            Consumer<RecipeEntry> adder
    ) {
        adder.accept(new RecipeEntry(suffix, (mat, id) ->
                BCLRecipeBuilder
                        .crafting(id, parentMaterial.getBlock(suffix))
                        .setOutputCount(3)
                        .setShape("###", "###", " I ")
                        .addMaterial('#', parentMaterial.getBlock(WoodSlots.PLANKS))
                        .addMaterial('I', Items.STICK)
                        .setGroup("sign")
                        .setCategory(RecipeCategory.DECORATIONS)
                        .build()
        ));
    }
}
