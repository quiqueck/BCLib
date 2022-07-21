package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;

import java.util.*;

public class BiomesConfig extends PathConfig {

    private Map<BiomeAPI.BiomeType, List<String>> BIOME_INCLUDE_LIST = null;
    private Map<BiomeAPI.BiomeType, List<String>> BIOME_EXCLUDE_LIST = null;


    public static final BiomeAPI.BiomeType[] endTypes = {
            BiomeAPI.BiomeType.END_LAND,
            BiomeAPI.BiomeType.END_VOID,
            BiomeAPI.BiomeType.END_CENTER,
            BiomeAPI.BiomeType.END_BARRENS
    };

    public static final BiomeAPI.BiomeType[] netherTypes = {
            BiomeAPI.BiomeType.NETHER
    };

    private static final BiomeAPI.BiomeType[] includeTypes = all();
    private static final BiomeAPI.BiomeType[] excludeTypes = {BiomeAPI.BiomeType.NETHER, BiomeAPI.BiomeType.END};

    public BiomesConfig() {
        super(BCLib.MOD_ID, "biomes", true);
        for (var type : includeTypes) {
            keeper.registerEntry(
                    new ConfigKey(type.getName(), "force_include"),
                    new ConfigKeeper.StringArrayEntry(Collections.EMPTY_LIST)
            );
        }
        for (var type : excludeTypes) {
            keeper.registerEntry(
                    new ConfigKey(type.getName(), "force_exclude"),
                    new ConfigKeeper.StringArrayEntry(Collections.EMPTY_LIST)
            );
        }
    }

    private static BiomeAPI.BiomeType[] all() {
        BiomeAPI.BiomeType[] res = new BiomeAPI.BiomeType[endTypes.length + netherTypes.length];
        System.arraycopy(netherTypes, 0, res, 0, netherTypes.length);
        System.arraycopy(endTypes, 0, res, netherTypes.length, endTypes.length);
        return res;
    }

    private List<String> getBiomeIncludeList(BiomeAPI.BiomeType type) {
        var entry = getEntry(
                "force_include",
                type.getName(),
                ConfigKeeper.StringArrayEntry.class
        );
        if (entry == null)
            return List.of();
        return entry.getValue();
    }

    private List<String> getBiomeExcludeList(BiomeAPI.BiomeType type) {
        var entry = getEntry(
                "force_exclude",
                type.getName(),
                ConfigKeeper.StringArrayEntry.class
        );
        if (entry == null)
            return List.of();
        return entry.getValue();
    }

    public List<String> getIncludeMatching(BiomeAPI.BiomeType type) {
        return getBiomeIncludeMap().entrySet()
                                   .stream()
                                   .filter(e -> e.getKey().is(type))
                                   .map(e -> e.getValue())
                                   .flatMap(Collection::stream)
                                   .toList();
    }

    public List<String> getExcludeMatching(BiomeAPI.BiomeType type) {
        return getBiomeExcludeMap().entrySet()
                                   .stream()
                                   .filter(e -> e.getKey().is(type))
                                   .map(e -> e.getValue())
                                   .flatMap(Collection::stream)
                                   .toList();
    }


    public Map<BiomeAPI.BiomeType, List<String>> getBiomeIncludeMap() {
        if (BIOME_INCLUDE_LIST == null) {
            BIOME_INCLUDE_LIST = new HashMap<>();
            for (BiomeAPI.BiomeType type : includeTypes) {
                BIOME_INCLUDE_LIST.put(type, getBiomeIncludeList(type));
            }
        }
        return BIOME_INCLUDE_LIST;
    }

    public Map<BiomeAPI.BiomeType, List<String>> getBiomeExcludeMap() {
        if (BIOME_EXCLUDE_LIST == null) {
            BIOME_EXCLUDE_LIST = new HashMap<>();
            for (BiomeAPI.BiomeType type : excludeTypes) {
                BIOME_EXCLUDE_LIST.put(type, getBiomeExcludeList(type));
            }
        }
        return BIOME_EXCLUDE_LIST;
    }
}
