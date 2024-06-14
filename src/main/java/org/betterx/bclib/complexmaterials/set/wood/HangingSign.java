package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.blocks.signs.BaseHangingSignBlock;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.MaterialSlot;
import org.betterx.bclib.complexmaterials.entry.RecipeEntry;
import org.betterx.wover.recipe.api.RecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
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
                (complexMaterial, settings) -> new BaseHangingSignBlock.Wood(parentMaterial.woodType)
        );

        var wallSignEntry = new BlockEntry(
                WALL_SUFFFIX,
                false,
                (complexMaterial, settings) -> {
                    if (complexMaterial.getBlock(suffix) instanceof BaseHangingSignBlock sign) {
                        return sign.getWallSignBlock();
                    }
                    return null;
                }
        );

        adder.accept(signEntry);
        adder.accept(wallSignEntry);
    }

    @Override
    public void addRecipeEntry(
            WoodenComplexMaterial parentMaterial,
            Consumer<RecipeEntry> adder
    ) {
        adder.accept(new RecipeEntry(suffix, (ctx, mat, id) ->
                RecipeBuilder
                        .crafting(id, parentMaterial.getBlock(suffix))
                        .outputCount(3)
                        .shape("I I", "###", "###")
                        .addMaterial('#', parentMaterial.getBlock(WoodSlots.STRIPPED_LOG))
                        .addMaterial('I', Items.CHAIN)
                        .group("sign")
                        .category(RecipeCategory.DECORATIONS)
                        .build(ctx)
        ));
    }
}
