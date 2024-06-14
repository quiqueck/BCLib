package org.betterx.worlds.together.tag.v3;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

@Deprecated(forRemoval = true)
public class CommonBiomeTags {
    public static final TagKey<Biome> IS_END_CENTER = org.betterx.wover.tag.api.predefined.CommonBiomeTags.IS_END_CENTER;
    public static final TagKey<Biome> IS_END_HIGHLAND = org.betterx.wover.tag.api.predefined.CommonBiomeTags.IS_END_HIGHLAND;
    public static final TagKey<Biome> IS_END_MIDLAND = org.betterx.wover.tag.api.predefined.CommonBiomeTags.IS_END_MIDLAND;
    public static final TagKey<Biome> IS_END_BARRENS = org.betterx.wover.tag.api.predefined.CommonBiomeTags.IS_END_BARRENS;
    public static final TagKey<Biome> IS_SMALL_END_ISLAND = org.betterx.wover.tag.api.predefined.CommonBiomeTags.IS_SMALL_END_ISLAND;

    static void prepareTags() {
        // NO-OP
    }
}
