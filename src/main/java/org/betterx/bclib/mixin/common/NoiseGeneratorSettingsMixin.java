package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.generator.BCLChunkGenerator;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseGeneratorSettings.class)
public abstract class NoiseGeneratorSettingsMixin {
    @Shadow
    static protected Holder<NoiseGeneratorSettings> register(
            Registry<NoiseGeneratorSettings> registry,
            ResourceKey<NoiseGeneratorSettings> resourceKey,
            NoiseGeneratorSettings noiseGeneratorSettings
    ) {
        return null;
    }

    ;

    @Inject(method = "bootstrap", at = @At("HEAD"))
    private static void bcl_addNoiseGenerators(
            Registry<NoiseGeneratorSettings> registry,
            CallbackInfoReturnable<Holder<NoiseGeneratorSettings>> cir
    ) {
        register(registry, BCLChunkGenerator.AMPLIFIED_NETHER, BCLChunkGenerator.amplifiedNether());
    }
}
