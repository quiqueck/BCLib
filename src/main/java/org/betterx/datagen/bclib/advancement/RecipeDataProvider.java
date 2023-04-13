package org.betterx.datagen.bclib.advancement;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.recipes.BCLRecipeBuilder;
import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import java.util.List;

public class RecipeDataProvider extends org.betterx.bclib.api.v3.datagen.RecipeDataProvider {
    public RecipeDataProvider(FabricDataOutput output) {
        super(List.of(BCLib.MOD_ID, WorldsTogether.MOD_ID), output);
    }

    public static void createTestRecipes() {
        BCLRecipeBuilder
                .crafting(BCLib.makeID("test_star"), Items.NETHER_STAR)
                .setOutputCount(1)
                .setShape("ggg", "glg", "ggg")
                .addMaterial('g', Items.GLASS_PANE)
                .addMaterial('l', Items.LAPIS_LAZULI)
                .setCategory(RecipeCategory.TOOLS)
                .build();
    }

}
