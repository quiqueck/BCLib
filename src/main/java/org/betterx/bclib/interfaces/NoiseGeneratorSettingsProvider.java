package org.betterx.bclib.interfaces;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public interface NoiseGeneratorSettingsProvider {
    NoiseGeneratorSettings bclib_getNoiseGeneratorSettings();
    Holder<NoiseGeneratorSettings> bclib_getNoiseGeneratorSettingHolders();
}
