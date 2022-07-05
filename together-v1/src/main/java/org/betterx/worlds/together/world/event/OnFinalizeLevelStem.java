package org.betterx.worlds.together.world.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

@FunctionalInterface
public interface OnFinalizeLevelStem {
    void now(WorldGenSettings worldGenSettings, ResourceKey<LevelStem> dimensionKey, LevelStem stem);
}
