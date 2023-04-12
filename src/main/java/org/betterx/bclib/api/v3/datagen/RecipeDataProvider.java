package org.betterx.bclib.api.v3.datagen;

import org.betterx.bclib.BCLib;

import net.minecraft.data.recipes.FinishedRecipe;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class RecipeDataProvider extends FabricRecipeProvider {
    private static List<DatapackRecipeBuilder> RECIPES;

    @Nullable
    protected final List<String> modIDs;

    public RecipeDataProvider(@Nullable List<String> modIDs, FabricDataOutput output) {
        super(output);
        this.modIDs = modIDs;
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> exporter) {
        if (RECIPES == null) return;

        for (var r : RECIPES) {
            if (modIDs.size() == 0 || modIDs.indexOf(r.getNamespace()) >= 0) {
                r.build(exporter);
            }
        }
    }

    @ApiStatus.Internal
    public static void register(DatapackRecipeBuilder builder) {
        //thi is only used withe the Data Generator, so we do not keep this list on a regular run
        if (!BCLib.isDatagen()) {
            return;
        }
        if (RECIPES == null) RECIPES = new ArrayList<>();
        RECIPES.add(builder);
    }
}
