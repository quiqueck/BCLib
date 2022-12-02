package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.Biomes;

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
        
        final BCLBiome NETHER_WASTES_BIOME = InternalBiomeAPI.wrapNativeBiome(
                Biomes.NETHER_WASTES,
                InternalBiomeAPI.OTHER_NETHER
        );
        final BCLBiome CRIMSON_FOREST_BIOME = InternalBiomeAPI.wrapNativeBiome(
                Biomes.CRIMSON_FOREST,
                InternalBiomeAPI.OTHER_NETHER
        );
        final BCLBiome WARPED_FOREST_BIOME = InternalBiomeAPI.wrapNativeBiome(
                Biomes.WARPED_FOREST,
                InternalBiomeAPI.OTHER_NETHER
        );
        final BCLBiome SOUL_SAND_VALLEY_BIOME = InternalBiomeAPI.wrapNativeBiome(
                Biomes.SOUL_SAND_VALLEY,
                InternalBiomeAPI.OTHER_NETHER
        );
        final BCLBiome BASALT_DELTAS_BIOME = InternalBiomeAPI.wrapNativeBiome(
                Biomes.BASALT_DELTAS,
                InternalBiomeAPI.OTHER_NETHER
        );


        final BCLBiome END_MIDLANDS = InternalBiomeAPI.wrapNativeBiome(
                Biomes.END_MIDLANDS,
                0.5F,
                InternalBiomeAPI.OTHER_END_LAND
        );

        final BCLBiome END_HIGHLANDS = InternalBiomeAPI.wrapNativeBiome(
                Biomes.END_HIGHLANDS,
                END_MIDLANDS,
                8,
                0.5F,
                InternalBiomeAPI.OTHER_END_LAND
        );


        final BCLBiome END_BARRENS = InternalBiomeAPI.wrapNativeBiome(
                Biomes.END_BARRENS,
                InternalBiomeAPI.OTHER_END_BARRENS
        );

        final BCLBiome SMALL_END_ISLANDS = InternalBiomeAPI.wrapNativeBiome(
                Biomes.SMALL_END_ISLANDS,
                InternalBiomeAPI.OTHER_END_VOID
        );
        Registry.register(reg, SMALL_END_ISLANDS.getBCLBiomeKey(), SMALL_END_ISLANDS);
        Registry.register(reg, END_BARRENS.getBCLBiomeKey(), END_BARRENS);
        Registry.register(reg, END_HIGHLANDS.getBCLBiomeKey(), END_HIGHLANDS);
        Registry.register(reg, END_MIDLANDS.getBCLBiomeKey(), END_MIDLANDS);
        Registry.register(reg, BCLBiomeRegistry.THE_END.getBCLBiomeKey(), BCLBiomeRegistry.THE_END);
        Registry.register(
                reg,
                BASALT_DELTAS_BIOME.getBCLBiomeKey(),
                BASALT_DELTAS_BIOME
        );
        Registry.register(
                reg,
                SOUL_SAND_VALLEY_BIOME.getBCLBiomeKey(),
                SOUL_SAND_VALLEY_BIOME
        );
        Registry.register(
                reg,
                WARPED_FOREST_BIOME.getBCLBiomeKey(),
                WARPED_FOREST_BIOME
        );
        Registry.register(
                reg,
                CRIMSON_FOREST_BIOME.getBCLBiomeKey(),
                CRIMSON_FOREST_BIOME
        );
        Registry.register(
                reg,
                NETHER_WASTES_BIOME.getBCLBiomeKey(),
                NETHER_WASTES_BIOME
        );
        return Registry.register(reg, BCLBiomeRegistry.EMPTY_BIOME.getBCLBiomeKey(), BCLBiomeRegistry.EMPTY_BIOME);
    }
}
