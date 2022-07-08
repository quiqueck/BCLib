package org.betterx.worlds.together.chunkgenerator;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;

public interface RestorableBiomeSource<B extends ChunkGenerator> {
    void restoreInitialBiomeSource(ResourceKey<LevelStem> dimensionKey);
}
