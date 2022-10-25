package org.betterx.bclib.mixin.common;

import net.minecraft.core.RegistryAccess;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryAccess.class)
public interface RegistryAccessMixin {
    //TODO: 1.19.3 Will probably be  a new custom data provider now
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
