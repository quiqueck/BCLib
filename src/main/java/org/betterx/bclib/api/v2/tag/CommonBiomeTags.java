package org.betterx.bclib.api.v2.tag;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * @deprecated Replaced by {@link org.betterx.worlds.together.tag.CommonBiomeTags}
 */
@Deprecated(forRemoval = true)
public class CommonBiomeTags {
    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.tag.CommonBiomeTags#IN_NETHER}
     **/
    @Deprecated(forRemoval = true)
    public static final TagKey<Biome> IN_NETHER = org.betterx.worlds.together.tag.CommonBiomeTags.IN_NETHER;
}
