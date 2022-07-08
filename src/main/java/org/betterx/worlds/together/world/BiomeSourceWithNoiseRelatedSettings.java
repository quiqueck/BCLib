package org.betterx.worlds.together.world;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public interface BiomeSourceWithNoiseRelatedSettings {
    void onLoadGeneratorSettings(NoiseGeneratorSettings generator);
}
