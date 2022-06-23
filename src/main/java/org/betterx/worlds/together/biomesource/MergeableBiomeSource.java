package org.betterx.worlds.together.biomesource;

import net.minecraft.world.level.biome.BiomeSource;

public interface MergeableBiomeSource<B extends BiomeSource> {

    /**
     * Returns a BiomeSource that merges the settings of this one with the Biomes (and possibly settings) from the
     * {@code inputBiomeSource}.
     *
     * @param inputBiomeSource The {@link BiomeSource} you want to copy
     * @return The merged or new BiomeSource
     */
    B mergeWithBiomeSource(BiomeSource inputBiomeSource);


}
