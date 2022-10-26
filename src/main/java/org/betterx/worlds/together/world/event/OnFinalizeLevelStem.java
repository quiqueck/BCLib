package org.betterx.worlds.together.world.event;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;

@FunctionalInterface
public interface OnFinalizeLevelStem {
    void now(Registry<LevelStem> dimensionRegistry, ResourceKey<LevelStem> dimensionKey, LevelStem stem);
}
