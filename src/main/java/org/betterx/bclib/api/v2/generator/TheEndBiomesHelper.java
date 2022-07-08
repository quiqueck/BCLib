package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.ApiStatus;


/**
 * Helper class until FAPI integrates <a href="https://github.com/FabricMC/fabric/pull/2369">this PR</a>
 */
public class TheEndBiomesHelper {
    @ApiStatus.Internal
    private static Map<BiomeAPI.BiomeType, Set<ResourceKey<Biome>>> END_BIOMES = new HashMap<>();

    @ApiStatus.Internal
    public static void add(BiomeAPI.BiomeType type, ResourceKey<Biome> biome) {
        if (biome == null) return;
        END_BIOMES.computeIfAbsent(type, t -> new HashSet<>()).add(biome);
    }

    private static boolean has(BiomeAPI.BiomeType type, ResourceKey<Biome> biome) {
        if (biome == null) return false;
        Set<ResourceKey<Biome>> set = END_BIOMES.get(type);
        if (set == null) return false;
        return set.contains(biome);
    }

    /**
     * Returns true if the given biome was added as a main end Biome in the end, considering the Vanilla end biomes,
     * and any biomes added to the End by mods.
     *
     * @param biome The biome to search for
     */
    public static boolean canGenerateAsMainIslandBiome(ResourceKey<Biome> biome) {
        return has(BiomeAPI.BiomeType.END_CENTER, biome);
    }

    /**
     * Returns true if the given biome was added as a small end islands Biome in the end, considering the Vanilla end biomes,
     * and any biomes added to the End by mods.
     *
     * @param biome The biome to search for
     */
    public static boolean canGenerateAsSmallIslandsBiome(ResourceKey<Biome> biome) {
        return has(BiomeAPI.BiomeType.END_VOID, biome);
    }

    /**
     * Returns true if the given biome was added as a Highland Biome in the end, considering the Vanilla end biomes,
     * and any biomes added to the End by mods.
     *
     * @param biome The biome to search for
     */
    public static boolean canGenerateAsHighlandsBiome(ResourceKey<Biome> biome) {
        return has(BiomeAPI.BiomeType.END_LAND, biome);
    }

    /**
     * Returns true if the given biome was added as midland biome in the end, considering the Vanilla end biomes,
     * and any biomes added to the End as midland biome by mods.
     *
     * @param biome The biome to search for
     */
    public static boolean canGenerateAsEndMidlands(ResourceKey<Biome> biome) {
        return false;
    }

    /**
     * Returns true if the given biome was added as barrens biome in the end, considering the Vanilla end biomes,
     * and any biomes added to the End as barrens biome by mods.
     *
     * @param biome The biome to search for
     */
    public static boolean canGenerateAsEndBarrens(ResourceKey<Biome> biome) {
        return has(BiomeAPI.BiomeType.END_BARRENS, biome);
    }

    public static boolean canGenerateInEnd(ResourceKey<Biome> biome) {
        return canGenerateAsHighlandsBiome(biome)
                || canGenerateAsEndBarrens(biome)
                || canGenerateAsEndMidlands(biome)
                || canGenerateAsSmallIslandsBiome(biome)
                || canGenerateAsMainIslandBiome(biome);
    }
}
