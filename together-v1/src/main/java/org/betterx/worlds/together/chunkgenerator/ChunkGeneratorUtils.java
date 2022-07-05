package org.betterx.worlds.together.chunkgenerator;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public class ChunkGeneratorUtils {
    public static void restoreOriginalBiomeSourceInAllDimension(WorldGenSettings settings) {
        for (var entry : settings.dimensions().entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            LevelStem stem = entry.getValue();

            if (stem.generator() instanceof RestorableBiomeSource<?> generator) {
                generator.restoreInitialBiomeSource(key);
            }
        }
    }


}
