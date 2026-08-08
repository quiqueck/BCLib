package org.betterx.bclib.mixin.common;

import net.minecraft.resources.RegistryDataLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vestigial: BCLib used to prepend its own biome registry to {@code RegistryDataLoader.WORLDGEN_REGISTRIES}
 * from the class initializer. Biome handling moved to WoVer long ago and the body has been commented out ever
 * since; only the {@code <clinit>} hook is kept so the ordering guarantee can be restored in place if it is
 * ever needed again.
 * <p>
 * The {@code wt_set_WORLDGEN_REGISTRIES} {@code @Accessor} that used to sit here was removed for 26.3: the
 * registry lists were split into {@code WORLD_REGISTRIES} / {@code DIMENSION_REGISTRIES} /
 * {@code RELOADABLE_REGISTRIES} / {@code SYNCHRONIZED_REGISTRIES} and there is no {@code WORLDGEN_REGISTRIES}
 * field any more, so the accessor could not have applied. It had no callers (its only use was inside the
 * commented-out block below).
 */
@Mixin(value = RegistryDataLoader.class, priority = 500)
public class RegistryDataLoaderMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void bcl_init(CallbackInfo ci) {
        //we need this to ensure, that the BCL-Biome Registry is loaded at the correct time
        //We use WoVer for biom handling now...
//        List<RegistryDataLoader.RegistryData<?>> enhanced = new ArrayList(RegistryDataLoader.WORLD_REGISTRIES.size() + 1);
//        enhanced.add(new RegistryDataLoader.RegistryData<>(
//                BCLBiomeRegistry.BCL_BIOMES_REGISTRY, BiomeData.CODEC
//        ));
//        enhanced.addAll(RegistryDataLoader.WORLD_REGISTRIES);
//        wt_set_WORLD_REGISTRIES(enhanced);
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
