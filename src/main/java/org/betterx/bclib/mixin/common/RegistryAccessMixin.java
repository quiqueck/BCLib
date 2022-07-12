package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeData;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(RegistryAccess.class)
public interface RegistryAccessMixin {

//    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;make(Ljava/util/function/Supplier;)Ljava/lang/Object;"))
//    private static Supplier<ImmutableMap<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>>> together_addRegistry(
//            Supplier<ImmutableMap<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>>> supplier
//    ) {
//        return () -> {
//            ImmutableMap.Builder<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>> builder = ImmutableMap.builder();
//            //Make sure this gets added before WORLD_PRESETS
//            put(builder, BCLBiomeRegistry.BCL_BIOMES_REGISTRY, BiomeData.CODEC);
//
//            Map<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>> res = supplier.get();
//            builder.putAll(res);
//
//            return builder.build();
//        };
//    }

    @Shadow
    static <E> void put(
            ImmutableMap.Builder<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>> builder,
            ResourceKey<? extends Registry<E>> resourceKey,
            Codec<E> codec
    ) {
        throw new RuntimeException("Shadowed Call");
    }

}
