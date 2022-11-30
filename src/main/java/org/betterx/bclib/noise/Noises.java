package org.betterx.bclib.noise;

import org.betterx.bclib.BCLib;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.HashMap;
import java.util.Map;

public class Noises {
    private static final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances = new HashMap<>();
    public static final ResourceKey<NormalNoise.NoiseParameters> ROUGHNESS_NOISE = createKey(BCLib.makeID(
            "roughness_noise"));

    public static ResourceKey<NormalNoise.NoiseParameters> createKey(ResourceLocation loc) {
        return ResourceKey.create(Registries.NOISE, loc);
    }

    public static NormalNoise createNoise(
            Registry<NormalNoise.NoiseParameters> registry,
            RandomSource randomSource,
            ResourceKey<NormalNoise.NoiseParameters> resourceKey
    ) {
        Holder<NormalNoise.NoiseParameters> holder = registry.getHolderOrThrow(resourceKey);
        return NormalNoise.create(randomSource, holder.value());
    }

    public static NormalNoise getOrCreateNoise(
            RegistryAccess registryAccess,
            RandomSource randomSource,
            ResourceKey<NormalNoise.NoiseParameters> noise
    ) {
        final Registry<NormalNoise.NoiseParameters> registry = registryAccess.registryOrThrow(Registries.NOISE);
        return noiseIntances.computeIfAbsent(noise, (key) -> createNoise(registry, randomSource, noise));
    }
}
