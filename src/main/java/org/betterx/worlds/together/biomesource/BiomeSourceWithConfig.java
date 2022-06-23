package org.betterx.worlds.together.biomesource;

import org.betterx.worlds.together.biomesource.config.BiomeSourceConfig;

import net.minecraft.world.level.biome.BiomeSource;

public interface BiomeSourceWithConfig<B extends BiomeSource, C extends BiomeSourceConfig<B>> {
    C getTogetherConfig();
    void setTogetherConfig(C newConfig);
}
