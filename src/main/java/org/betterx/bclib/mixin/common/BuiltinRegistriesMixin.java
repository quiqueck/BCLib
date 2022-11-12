package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public abstract class BuiltinRegistriesMixin {
    @Shadow
    protected static <T, R extends WritableRegistry<T>> R internalRegister(
            ResourceKey<? extends Registry<T>> resourceKey,
            R writableRegistry,
            BuiltInRegistries.RegistryBootstrap<T> registryBootstrap,
            Lifecycle lifecycle
    ) {
        throw new RuntimeException("Shadowed Call");
    }

    //this needs to be added BEFORE the WORLD_PRESET-Registry. Otherwise decoding will fail!
    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/BuiltinRegistries;registerSimple(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/data/BuiltinRegistries$RegistryBootstrap;)Lnet/minecraft/core/Registry;", ordinal = 0))
    private static void bcl_registerBuiltin(CallbackInfo ci) {
        BCLBiomeRegistry.BUILTIN_BCL_BIOMES = internalRegister(
                BCLBiomeRegistry.BCL_BIOMES_REGISTRY,
                (MappedRegistry) BCLBiomeRegistry.BUILTIN_BCL_BIOMES,
                BCLBiomeRegistry::bootstrap,
                Lifecycle.stable()
        );
    }
}
