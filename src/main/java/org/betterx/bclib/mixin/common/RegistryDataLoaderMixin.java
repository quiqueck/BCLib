package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeData;

import net.minecraft.resources.RegistryDataLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
    @Accessor("WORLDGEN_REGISTRIES")
    @Mutable
    static void wt_set_WORLDGEN_REGISTRIES(List<RegistryDataLoader.RegistryData<?>> list) {
        //SHADOWED
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void wt_init(CallbackInfo ci) {
        //we need this to ensure, that the BCL-Biome Registry is loaded at the correct time
        List<RegistryDataLoader.RegistryData<?>> enhanced = new ArrayList(RegistryDataLoader.WORLDGEN_REGISTRIES.size() + 1);
        enhanced.addAll(RegistryDataLoader.WORLDGEN_REGISTRIES);
        enhanced.add(new RegistryDataLoader.RegistryData<>(
                BCLBiomeRegistry.BCL_BIOMES_REGISTRY, BiomeData.CODEC
        ));
        wt_set_WORLDGEN_REGISTRIES(enhanced);
    }
}
