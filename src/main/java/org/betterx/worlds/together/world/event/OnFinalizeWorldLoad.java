package org.betterx.worlds.together.world.event;

import net.minecraft.world.level.levelgen.WorldGenSettings;

public interface OnFinalizeWorldLoad {
    void done(WorldGenSettings settings);
}
