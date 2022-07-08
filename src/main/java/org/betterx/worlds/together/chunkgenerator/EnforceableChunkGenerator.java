package org.betterx.worlds.together.chunkgenerator;

import org.betterx.worlds.together.biomesource.BiomeSourceFromRegistry;
import org.betterx.worlds.together.biomesource.BiomeSourceWithConfig;
import org.betterx.worlds.together.biomesource.MergeableBiomeSource;

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

    default boolean togetherShouldRepair(ChunkGenerator chunkGenerator) {
        ChunkGenerator self = (ChunkGenerator) this;
        if (this == chunkGenerator || chunkGenerator == null) return false;

        BiomeSource one = self.getBiomeSource();
        BiomeSource two = chunkGenerator.getBiomeSource();
        if (one == two) return false;

        if (one instanceof BiomeSourceWithConfig<?, ?> ba && two instanceof BiomeSourceWithConfig<?, ?> bb) {
            if (!ba.getTogetherConfig().couldSetWithoutRepair(bb.getTogetherConfig()))
                return true;
        }
        if (one instanceof BiomeSourceFromRegistry ba && two instanceof BiomeSourceFromRegistry bb) {
            if (ba.togetherBiomeSourceContentChanged(bb))
                return true;
        }
        if (one instanceof MergeableBiomeSource ba) {
            if (ba.togetherShouldMerge(two))
                return true;
        }

        return !one.getClass().isAssignableFrom(two.getClass()) && !two.getClass().isAssignableFrom(one.getClass());
    }
}
