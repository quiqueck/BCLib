package org.betterx.bclib.complexmaterials.set.wood;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.complexmaterials.WoodenComplexMaterial;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.SimpleMaterialSlot;
import org.betterx.bclib.furniture.block.BaseTaburet;
import org.betterx.wover.recipe.api.BaseRecipeBuilder;
import org.betterx.wover.recipe.api.CraftingRecipeBuilder;
import org.betterx.wover.recipe.api.RecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Taburet extends SimpleMaterialSlot<WoodenComplexMaterial> {
    public Taburet() {
        super("taburet");
    }

    public static void makeTaburetRecipe(RecipeOutput context, ResourceLocation id, Block taburet, Block planks) {
        CraftingRecipeBuilder craftingRecipeBuilder1 = RecipeBuilder.crafting(id, taburet);
        CraftingRecipeBuilder craftingRecipeBuilder = craftingRecipeBuilder1.shape("##", "II")
                                                                            .addMaterial('#', planks)
                                                                            .addMaterial('I', Items.STICK);
        BaseRecipeBuilder<CraftingRecipeBuilder> craftingRecipeBuilderBaseRecipeBuilder = craftingRecipeBuilder.group("taburet");
        craftingRecipeBuilderBaseRecipeBuilder.category(RecipeCategory.DECORATIONS)
                                              .build(context);
    }

    @Override
    protected void modifyBlockEntry(WoodenComplexMaterial parentMaterial, @NotNull BlockEntry entry) {
        entry.setBlockTags(BlockTags.MINEABLE_WITH_AXE);
    }

    @Override
    protected @NotNull Block createBlock(
            WoodenComplexMaterial parentMaterial, BlockBehaviour.Properties settings
    ) {
        return new BaseTaburet.Wood(parentMaterial.getBlock(WoodSlots.PLANKS));
    }

    @Override
    protected @Nullable void makeRecipe(RecipeOutput context, ComplexMaterial parentMaterial, ResourceLocation id) {
        Taburet.makeTaburetRecipe(context, id, parentMaterial.getBlock(suffix), parentMaterial.getBlock(WoodSlots.SLAB));
    }
}
