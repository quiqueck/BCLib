package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.surfaceRules.AssignedSurfaceRule;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

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
public abstract class RegistryDataLoaderMixin {
    @Accessor("WORLDGEN_REGISTRIES")
    @Mutable
    static void wt_set_WORLDGEN_REGISTRIES(List<RegistryDataLoader.RegistryData<?>> list) {
        //SHADOWED
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void wt_init(CallbackInfo ci) {
        List<RegistryDataLoader.RegistryData<?>> enhanced = new ArrayList(RegistryDataLoader.WORLDGEN_REGISTRIES.size() + 1);
        enhanced.addAll(RegistryDataLoader.WORLDGEN_REGISTRIES);
        enhanced.add(new RegistryDataLoader.RegistryData<>(
                SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
                AssignedSurfaceRule.CODEC
        ));
        wt_set_WORLDGEN_REGISTRIES(enhanced);
    }
}
