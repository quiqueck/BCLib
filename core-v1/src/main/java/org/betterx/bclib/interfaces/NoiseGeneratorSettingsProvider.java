package org.betterx.bclib.interfaces;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface NoiseGeneratorSettingsProvider {
    NoiseGeneratorSettings bclib_getNoiseGeneratorSettings();
    Holder<NoiseGeneratorSettings> bclib_getNoiseGeneratorSettingHolders();

    Registry<NormalNoise.NoiseParameters> bclib_getNoises();
}
