package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;

import net.minecraft.data.worldgen.BootstapContext;

public class VanillaBCLBiomesDataProvider {
    public static void bootstrap(BootstapContext<BCLBiome> ctx) {
        ctx.register(BiomeAPI.SMALL_END_ISLANDS.getBCLBiomeKey(), BiomeAPI.SMALL_END_ISLANDS);
        ctx.register(BiomeAPI.END_BARRENS.getBCLBiomeKey(), BiomeAPI.END_BARRENS);
        ctx.register(BiomeAPI.END_HIGHLANDS.getBCLBiomeKey(), BiomeAPI.END_HIGHLANDS);
        ctx.register(BiomeAPI.END_MIDLANDS.getBCLBiomeKey(), BiomeAPI.END_MIDLANDS);
        ctx.register(BiomeAPI.THE_END.getBCLBiomeKey(), BiomeAPI.THE_END);
        ctx.register(
                BiomeAPI.BASALT_DELTAS_BIOME.getBCLBiomeKey(),
                BiomeAPI.BASALT_DELTAS_BIOME
        );
        ctx.register(
                BiomeAPI.SOUL_SAND_VALLEY_BIOME.getBCLBiomeKey(),
                BiomeAPI.SOUL_SAND_VALLEY_BIOME
        );
        ctx.register(
                BiomeAPI.WARPED_FOREST_BIOME.getBCLBiomeKey(),
                BiomeAPI.WARPED_FOREST_BIOME
        );
        ctx.register(
                BiomeAPI.CRIMSON_FOREST_BIOME.getBCLBiomeKey(),
                BiomeAPI.CRIMSON_FOREST_BIOME
        );
        ctx.register(
                BiomeAPI.NETHER_WASTES_BIOME.getBCLBiomeKey(),
                BiomeAPI.NETHER_WASTES_BIOME
        );
        ctx.register(BCLBiomeRegistry.EMPTY_BIOME.getBCLBiomeKey(), BCLBiomeRegistry.EMPTY_BIOME);
    }
}
