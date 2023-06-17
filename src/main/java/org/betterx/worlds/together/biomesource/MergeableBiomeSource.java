package org.betterx.worlds.together.biomesource;

import org.betterx.bclib.BCLib;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

import java.util.Set;

public interface MergeableBiomeSource<B extends BiomeSource> {
    default boolean togetherShouldMerge(BiomeSource inputBiomeSource) {
        Set<Holder<Biome>> mySet = ((B) this).possibleBiomes();
        try {
            Set<Holder<Biome>> otherSet = inputBiomeSource.possibleBiomes();

            if (otherSet.size() != mySet.size()) return true;

            for (Holder<Biome> b : mySet) {
                if (!otherSet.contains(b))
                    return true;
            }
        } catch (RuntimeException e) {
            BCLib.LOGGER.error("Failed to merge BiomeSource", e);
        } catch (Exception e) {
            BCLib.LOGGER.error("Failed to merge BiomeSource", e);
        }

        return false;
    }

    /**
     * Returns a BiomeSource that merges the settings of this one with the Biomes (and possibly settings) from the
     * {@code inputBiomeSource}.
     *
     * @param inputBiomeSource The {@link BiomeSource} you want to copy
     * @return The merged or new BiomeSource
     */
    B mergeWithBiomeSource(BiomeSource inputBiomeSource);


}
