package org.betterx.datagen.bclib.tests;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.recipes.BCLRecipeBuilder;
import org.betterx.bclib.recipes.GridRecipe;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;

import java.util.function.Consumer;

public class TestRecipes extends FabricRecipeProvider {
    public TestRecipes(FabricDataOutput output) {
        super(output);
    }

    final GridRecipe WONDER = BCLRecipeBuilder
            .crafting(BCLib.makeID("test_star"), Items.NETHER_STAR)
            .setOutputCount(1)
            .setShape("ggg", "glg", "ggg")
            .addMaterial('g', Items.GLASS_PANE)
            .addMaterial('l', Items.LAPIS_LAZULI)
            .setCategory(RecipeCategory.TOOLS)
            .build();

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> exporter) {
        GridRecipe.registerRecipes(exporter);
    }
}
