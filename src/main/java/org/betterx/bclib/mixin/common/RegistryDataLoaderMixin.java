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

@Mixin(value = RegistryDataLoader.class, priority = 500)
public class RegistryDataLoaderMixin {
    @Accessor("WORLDGEN_REGISTRIES")
    @Mutable
    static void wt_set_WORLDGEN_REGISTRIES(List<RegistryDataLoader.RegistryData<?>> list) {
        //SHADOWED
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void bcl_init(CallbackInfo ci) {
        //we need this to ensure, that the BCL-Biome Registry is loaded at the correct time
        List<RegistryDataLoader.RegistryData<?>> enhanced = new ArrayList(RegistryDataLoader.WORLDGEN_REGISTRIES.size() + 1);
        enhanced.add(new RegistryDataLoader.RegistryData<>(
                BCLBiomeRegistry.BCL_BIOMES_REGISTRY, BiomeData.CODEC
        ));
        enhanced.addAll(RegistryDataLoader.WORLDGEN_REGISTRIES);
        wt_set_WORLDGEN_REGISTRIES(enhanced);
    }

//    // Fabric force changes the directory path for all modded registries to be prefixed with the mod id.
//    // We do not want this for our BCL-Biome/Surface Rule Registry, so we remove the prefix here.
//    @Inject(method = "registryDirPath", at = @At("RETURN"), cancellable = true)
//    private static void bcl_prependDirectoryWithNamespace(ResourceLocation id, CallbackInfoReturnable<String> info) {
//        if (id.getNamespace().equals(WorldsTogether.MOD_ID) || id.getNamespace().equals(BCLib.MOD_ID)) {
//            info.setReturnValue(info.getReturnValue());
//        }
//    }
}
