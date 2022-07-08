package org.betterx.worlds.together.biomesource;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;

import java.util.Set;

public interface BiomeSourceFromRegistry {
    Registry<Biome> getBiomeRegistry();
    Set<Holder<Biome>> possibleBiomes();

    default boolean sameRegistryButDifferentBiomes(BiomeSourceFromRegistry other) {
        if (other.getBiomeRegistry() == getBiomeRegistry()) {
            Set<Holder<Biome>> mySet = this.possibleBiomes();
            Set<Holder<Biome>> otherSet = other.possibleBiomes();
            if (otherSet.size() != mySet.size()) return true;
            for (Holder<Biome> b : mySet) {
                if (!otherSet.contains(b))
                    return true;
            }
        }

        return false;
    }
}
