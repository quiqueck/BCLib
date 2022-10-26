package org.betterx.worlds.together.world.event;

import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;

public interface OnFinalizeWorldLoad {
    void done(Registry<LevelStem> dimensionRegistry);
}
