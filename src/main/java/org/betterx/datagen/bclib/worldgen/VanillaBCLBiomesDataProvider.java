package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;

import net.minecraft.core.Registry;

public class VanillaBCLBiomesDataProvider {
    private static boolean didBootstrap = false;

    public static void create() {
        BCLBiomeRegistry.prepareForDatagen();
        bootstrap(BCLBiomeRegistry.BUILTIN_BCL_BIOMES);
    }

    public static BCLBiome bootstrap(Registry<BCLBiome> reg) {
        if (didBootstrap) return BCLBiomeRegistry.EMPTY_BIOME;
        didBootstrap = true;


        Registry.register(reg, BiomeAPI.SMALL_END_ISLANDS.getBCLBiomeKey(), BiomeAPI.SMALL_END_ISLANDS);
        Registry.register(reg, BiomeAPI.END_BARRENS.getBCLBiomeKey(), BiomeAPI.END_BARRENS);
        Registry.register(reg, BiomeAPI.END_HIGHLANDS.getBCLBiomeKey(), BiomeAPI.END_HIGHLANDS);
        Registry.register(reg, BiomeAPI.END_MIDLANDS.getBCLBiomeKey(), BiomeAPI.END_MIDLANDS);
        Registry.register(reg, BiomeAPI.THE_END.getBCLBiomeKey(), BiomeAPI.THE_END);
        Registry.register(
                reg,
                BiomeAPI.BASALT_DELTAS_BIOME.getBCLBiomeKey(),
                BiomeAPI.BASALT_DELTAS_BIOME
        );
        Registry.register(
                reg,
                BiomeAPI.SOUL_SAND_VALLEY_BIOME.getBCLBiomeKey(),
                BiomeAPI.SOUL_SAND_VALLEY_BIOME
        );
        Registry.register(
                reg,
                BiomeAPI.WARPED_FOREST_BIOME.getBCLBiomeKey(),
                BiomeAPI.WARPED_FOREST_BIOME
        );
        Registry.register(
                reg,
                BiomeAPI.CRIMSON_FOREST_BIOME.getBCLBiomeKey(),
                BiomeAPI.CRIMSON_FOREST_BIOME
        );
        Registry.register(
                reg,
                BiomeAPI.NETHER_WASTES_BIOME.getBCLBiomeKey(),
                BiomeAPI.NETHER_WASTES_BIOME
        );
        return Registry.register(reg, BCLBiomeRegistry.EMPTY_BIOME.getBCLBiomeKey(), BCLBiomeRegistry.EMPTY_BIOME);
    }
}
