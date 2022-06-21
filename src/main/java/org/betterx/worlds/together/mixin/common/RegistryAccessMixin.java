package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.SurfaceRules;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(RegistryAccess.class)
public interface RegistryAccessMixin {

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;make(Ljava/util/function/Supplier;)Ljava/lang/Object;"))
    private static Supplier<ImmutableMap<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>>> together_addRegistry(
            Supplier<ImmutableMap<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>>> supplier
    ) {
        return () -> {
            Map<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>> res = supplier.get();
            ImmutableMap.Builder<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>> builder = ImmutableMap.builder();

            builder.putAll(res);
            put(builder, SurfaceRuleRegistry.SURFACE_RULES_REGISTRY, SurfaceRules.RuleSource.CODEC);
            return builder.build();
        };
    }

    @Shadow
    static <E> void put(
            ImmutableMap.Builder<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>> builder,
            ResourceKey<? extends Registry<E>> resourceKey,
            Codec<E> codec
    ) {
        throw new RuntimeException("Shadowed Call");
    }

}
