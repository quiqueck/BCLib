package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class VanillaBCLBiomesDataProvider {
    private static boolean didBootstrap = false;

    public static void create() {
        BCLBiomeRegistry.BUILTIN_BCL_BIOMES = BuiltInRegistries.registerSimple(
                BCLBiomeRegistry.BCL_BIOMES_REGISTRY,
                VanillaBCLBiomesDataProvider::bootstrap
        );

        bootstrap(BCLBiomeRegistry.BUILTIN_BCL_BIOMES);
    }

    public static BCLBiome bootstrap(Registry<BCLBiome> reg) {
        if (didBootstrap) return BCLBiomeRegistry.EMPTY_BIOME;
        didBootstrap = true;


        Registry.register(reg, BCLBiomeRegistry.SMALL_END_ISLANDS.getBCLBiomeKey(), BCLBiomeRegistry.SMALL_END_ISLANDS);
        Registry.register(reg, BCLBiomeRegistry.END_BARRENS.getBCLBiomeKey(), BCLBiomeRegistry.END_BARRENS);
        Registry.register(reg, BCLBiomeRegistry.END_HIGHLANDS.getBCLBiomeKey(), BCLBiomeRegistry.END_HIGHLANDS);
        Registry.register(reg, BCLBiomeRegistry.END_MIDLANDS.getBCLBiomeKey(), BCLBiomeRegistry.END_MIDLANDS);
        Registry.register(reg, BCLBiomeRegistry.THE_END.getBCLBiomeKey(), BCLBiomeRegistry.THE_END);
        Registry.register(
                reg,
                BCLBiomeRegistry.BASALT_DELTAS_BIOME.getBCLBiomeKey(),
                BCLBiomeRegistry.BASALT_DELTAS_BIOME
        );
        Registry.register(
                reg,
                BCLBiomeRegistry.SOUL_SAND_VALLEY_BIOME.getBCLBiomeKey(),
                BCLBiomeRegistry.SOUL_SAND_VALLEY_BIOME
        );
        Registry.register(
                reg,
                BCLBiomeRegistry.WARPED_FOREST_BIOME.getBCLBiomeKey(),
                BCLBiomeRegistry.WARPED_FOREST_BIOME
        );
        Registry.register(
                reg,
                BCLBiomeRegistry.CRIMSON_FOREST_BIOME.getBCLBiomeKey(),
                BCLBiomeRegistry.CRIMSON_FOREST_BIOME
        );
        Registry.register(
                reg,
                BCLBiomeRegistry.NETHER_WASTES_BIOME.getBCLBiomeKey(),
                BCLBiomeRegistry.NETHER_WASTES_BIOME
        );
        return Registry.register(reg, BCLBiomeRegistry.EMPTY_BIOME.getBCLBiomeKey(), BCLBiomeRegistry.EMPTY_BIOME);
    }
}
