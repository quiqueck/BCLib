package org.betterx.datagen.bclib.advancement;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.core.HolderLookup;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RecipeDataProvider extends org.betterx.bclib.api.v3.datagen.RecipeDataProvider {
    public RecipeDataProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(List.of(BCLib.MOD_ID, WorldsTogether.MOD_ID), output, registriesFuture);
    }
}
