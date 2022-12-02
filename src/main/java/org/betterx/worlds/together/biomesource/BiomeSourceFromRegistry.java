package org.betterx.worlds.together.biomesource;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

@Deprecated(forRemoval = true)
public interface BiomeSourceFromRegistry<T extends BiomeSource> {
    HolderGetter<Biome> getBiomeRegistry();
    boolean didBiomeRegistryChange();

    default <R extends BiomeSource> boolean togetherBiomeSourceContentChanged(BiomeSourceFromRegistry<R> other) {
        if (other.getBiomeRegistry() != getBiomeRegistry()) return true;
        if (other.didBiomeRegistryChange() || didBiomeRegistryChange()) return true;

        return false;
    }
}
