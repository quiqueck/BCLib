package org.betterx.worlds.together.chunkgenerator;

import org.betterx.worlds.together.surfaceRules.SurfaceRuleUtil;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

public interface InjectableSurfaceRules<G extends ChunkGenerator> {
    /**
     * Called when the Surface Rules for this BiomeSource need to be
     *
     * @param dimensionKey The Dimension for which this injection is performed
     */
    default void injectSurfaceRules(ResourceKey<LevelStem> dimensionKey) {
        if (this instanceof NoiseBasedChunkGenerator nbc) {
            SurfaceRuleUtil.injectSurfaceRules(dimensionKey, nbc.generatorSettings().value(), nbc.getBiomeSource());
        }
    }
}
