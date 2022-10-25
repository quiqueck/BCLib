package org.betterx.bclib.interfaces;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;

public interface NumericProvider {
    ResourceKey<Registry<Codec<? extends NumericProvider>>> NUMERIC_PROVIDER_REGISTRY = ResourceKey.createRegistryKey(
            BCLib.makeID("worldgen/numeric_provider"));
    Registry<Codec<? extends NumericProvider>> NUMERIC_PROVIDER = new MappedRegistry<>(
            NUMERIC_PROVIDER_REGISTRY,
            Lifecycle.experimental()
    );
    Codec<NumericProvider> CODEC = NUMERIC_PROVIDER.byNameCodec()
                                                   .dispatch(NumericProvider::pcodec, Function.identity());
    int getNumber(SurfaceRulesContextAccessor context);

    Codec<? extends NumericProvider> pcodec();
}
