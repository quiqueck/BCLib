package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.datagen.TagDataProvider;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BiomeDatagenProvider extends TagDataProvider<Biome> {
    public BiomeDatagenProvider(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(TagManager.BIOMES, List.of(BCLib.MOD_ID, WorldsTogether.MOD_ID, "c"), output, registriesFuture);
    }

    public static void bootstrap(BootstapContext<Biome> bootstrapContext) {
        
    }
}
