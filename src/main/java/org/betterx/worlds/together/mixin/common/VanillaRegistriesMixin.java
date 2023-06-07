package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VanillaRegistries.class)
public class VanillaRegistriesMixin {
    @Shadow
    @Final
    private static RegistrySetBuilder BUILDER;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void together_registerSurface(CallbackInfo ci) {
        //this code is only needed for the DataGen procedure...
//        BUILDER.add(
//                BCLBiomeRegistry.BCL_BIOMES_REGISTRY,
//                BCLBiomeRegistry::bootstrap
//        );

        BUILDER.add(
                SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
                SurfaceRuleRegistry::bootstrap
        );
    }
}
