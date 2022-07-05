package org.betterx.worlds.together.tag.v3;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class CommonBiomeTags {
    public static final TagKey<Biome> IN_NETHER = TagManager.BIOMES.makeCommonTag("in_nether");
    public static final TagKey<Biome> IN_END = TagManager.BIOMES.makeCommonTag("in_end");

    static void prepareTags() {
    }
}
