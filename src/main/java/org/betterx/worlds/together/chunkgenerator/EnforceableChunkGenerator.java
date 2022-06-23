package org.betterx.worlds.together.chunkgenerator;

import org.betterx.worlds.together.biomesource.BiomeSourceWithConfig;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public interface EnforceableChunkGenerator<G extends ChunkGenerator> {
    WorldGenSettings enforceGeneratorInWorldGenSettings(
            RegistryAccess access,
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            ChunkGenerator loadedChunkGenerator,
            WorldGenSettings settings
    );

    default boolean needsChunkGeneratorRepair(ChunkGenerator chunkGenerator) {
        ChunkGenerator self = (ChunkGenerator) this;
        if (this == chunkGenerator || chunkGenerator == null) return false;

        BiomeSource one = self.getBiomeSource();
        BiomeSource two = chunkGenerator.getBiomeSource();
        if (one == two) return false;

        if (one instanceof BiomeSourceWithConfig<?, ?> ba && two instanceof BiomeSourceWithConfig<?, ?> bb) {
            return !ba.getTogetherConfig().couldSetWithoutRepair(bb.getTogetherConfig());
        }
        return !one.getClass().isAssignableFrom(two.getClass()) && !two.getClass().isAssignableFrom(one.getClass());
    }
}
