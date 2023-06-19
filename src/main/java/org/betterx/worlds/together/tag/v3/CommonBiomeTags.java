package org.betterx.worlds.together.tag.v3;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class CommonBiomeTags {
    public static final TagKey<Biome> IS_END_CENTER = TagManager.BIOMES.makeCommonTag("is_end_center");
    public static final TagKey<Biome> IS_END_HIGHLAND = TagManager.BIOMES.makeCommonTag("is_end_highland");
    public static final TagKey<Biome> IS_END_MIDLAND = TagManager.BIOMES.makeCommonTag("is_end_midland");
    public static final TagKey<Biome> IS_END_BARRENS = TagManager.BIOMES.makeCommonTag("is_end_barrens");
    public static final TagKey<Biome> IS_SMALL_END_ISLAND = TagManager.BIOMES.makeCommonTag("is_small_end_island");

    static void prepareTags() {
        TagManager.BIOMES.add(IS_END_CENTER, Biomes.THE_END);
        TagManager.BIOMES.add(IS_END_HIGHLAND, Biomes.END_HIGHLANDS);
        TagManager.BIOMES.add(IS_END_MIDLAND, Biomes.END_MIDLANDS);
        TagManager.BIOMES.add(IS_END_BARRENS, Biomes.END_BARRENS);
        TagManager.BIOMES.add(IS_SMALL_END_ISLAND, Biomes.SMALL_END_ISLANDS);
    }
}
