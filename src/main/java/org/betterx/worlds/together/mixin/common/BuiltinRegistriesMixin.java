package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(BuiltinRegistries.class)
public abstract class BuiltinRegistriesMixin {

    @Shadow
    static protected <T> Registry<T> registerSimple(
            ResourceKey<? extends Registry<T>> resourceKey,
            Supplier<? extends Holder<? extends T>> supplier
    ) {
        throw new RuntimeException("Shadowed Call");
    }

    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private static void together_registerSurface(CallbackInfo ci) {
        SurfaceRuleRegistry.BUILTIN_SURFACE_RULES = registerSimple(
                SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
                SurfaceRuleRegistry::bootstrap
        );
    }

}
