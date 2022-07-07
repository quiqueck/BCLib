package org.betterx.bclib.api.v2.levelgen.biomes;

import com.mojang.serialization.Codec;
import net.minecraft.util.KeyDispatchDataCodec;

import java.util.function.Function;

public interface BiomeData {
    Codec<BCLBiome> CODEC = BCLBiomeRegistry
            .BIOME_CODECS
            .byNameCodec()
            .dispatch(b -> b.codec().codec(), Function.identity());

    KeyDispatchDataCodec<? extends BCLBiome> codec();
}
