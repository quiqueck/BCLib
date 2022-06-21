package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinRegistries.class)
public class BuiltinRegistriesMixin {

    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private static void together_registerSurface(CallbackInfo ci) {
        SurfaceRuleRegistry.SURFACE_RULES = (Registry<SurfaceRules.RuleSource>) registerSimple(
                SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
                SurfaceRuleRegistry::bootstrap
        );
    }

    @Shadow
    static protected <T> Registry<T> registerSimple(
            ResourceKey<? extends Registry<T>> resourceKey, BuiltinRegistries.RegistryBootstrap<T> registryBootstrap
    ) {
        throw new RuntimeException("Shadowed Call");
    }
}
