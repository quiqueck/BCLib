package org.betterx.datagen.bclib.tests;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.datagen.RecipeDataProvider;
import org.betterx.bclib.recipes.BCLRecipeBuilder;
import org.betterx.bclib.recipes.CraftingRecipeBuilder;
import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import java.util.List;

public class TestRecipes extends RecipeDataProvider {
    public TestRecipes(FabricDataOutput output) {
        super(List.of(BCLib.MOD_ID, WorldsTogether.MOD_ID), output);
    }

    final CraftingRecipeBuilder WONDER = BCLRecipeBuilder
            .crafting(BCLib.makeID("test_star"), Items.NETHER_STAR)
            .setOutputCount(1)
            .setShape("ggg", "glg", "ggg")
            .addMaterial('g', Items.GLASS_PANE)
            .addMaterial('l', Items.LAPIS_LAZULI)
            .setCategory(RecipeCategory.TOOLS)
            .build();

}
