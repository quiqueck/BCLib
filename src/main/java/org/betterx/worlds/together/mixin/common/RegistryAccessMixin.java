package org.betterx.worlds.together.mixin.common;

import net.minecraft.core.RegistryAccess;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryAccess.class)
public interface RegistryAccessMixin {

    //TODO: 1.19.3 This will probably be a new kind of DataProvider.
//    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;make(Ljava/util/function/Supplier;)Ljava/lang/Object;"))
//    private static Supplier<ImmutableMap<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>>> together_addRegistry(
//            Supplier<ImmutableMap<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>>> supplier
//    ) {
//
//        return () -> {
//            Map<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>> res = supplier.get();
//            ImmutableMap.Builder<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>> builder = ImmutableMap.builder();
//
//            builder.putAll(res);
//            put(builder, SurfaceRuleRegistry.SURFACE_RULES_REGISTRY, AssignedSurfaceRule.CODEC);
//            return builder.build();
//        };
//    }
//
//    @Shadow
//    static <E> void put(
//            ImmutableMap.Builder<ResourceKey<Registry<?>>, RegistryAccess.RegistryData<?>> builder,
//            ResourceKey<? extends Registry<E>> resourceKey,
//            Codec<E> codec
//    ) {
//        throw new RuntimeException("Shadowed Call");
//    }

}
