package org.betterx.bclib.mixin.common;

import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;
import org.betterx.bclib.interfaces.SurfaceProvider;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin implements SurfaceProvider, NoiseGeneratorSettingsProvider {
    @Final
    @Shadow
    protected Holder<NoiseGeneratorSettings> settings;


    @Override
    public NoiseGeneratorSettings bclib_getNoiseGeneratorSettings() {
        return settings.value();
    }

    @Override
    public Holder<NoiseGeneratorSettings> bclib_getNoiseGeneratorSettingHolders() {
        return settings;
    }
}
