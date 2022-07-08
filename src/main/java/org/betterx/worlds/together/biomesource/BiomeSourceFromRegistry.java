package org.betterx.worlds.together.biomesource;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

import java.util.Set;

public interface BiomeSourceFromRegistry<T extends BiomeSource> {
    Registry<Biome> getBiomeRegistry();

    default <R extends BiomeSource> boolean sameRegistryButDifferentBiomes(BiomeSourceFromRegistry<R> other) {
        if (other.getBiomeRegistry() == getBiomeRegistry()) {
            Set<Holder<Biome>> mySet = ((T) this).possibleBiomes();
            Set<Holder<Biome>> otherSet = ((R) other).possibleBiomes();
            if (otherSet.size() != mySet.size()) return true;
            for (Holder<Biome> b : mySet) {
                if (!otherSet.contains(b))
                    return true;
            }
        }

        return false;
    }
}
