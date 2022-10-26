package org.betterx.worlds.together.chunkgenerator;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

public class ChunkGeneratorUtils {
    public static void restoreOriginalBiomeSourceInAllDimension(Registry<LevelStem> dimensionRegistry) {
        for (var entry : dimensionRegistry.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            LevelStem stem = entry.getValue();

            if (stem.generator() instanceof RestorableBiomeSource<?> generator) {
                generator.restoreInitialBiomeSource(key);
            }
        }
    }


}
