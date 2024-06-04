package org.betterx.bclib.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class BCLParticleType {

    public static <T extends ParticleOptions> ParticleType<T> deserializer(
            MapCodec<T> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
    ) {
        return deserializer(false, codec, streamCodec);
    }

    public static <T extends ParticleOptions> ParticleType<T> deserializer(
            boolean overrideLimiter,
            MapCodec<T> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
    ) {
        return new ParticleType<T>(overrideLimiter) {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodec;
            }
        };
    }

    public static <T extends ParticleOptions> ParticleType<T> register(
            ResourceLocation location,
            MapCodec<T> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
    ) {
        return register(location, false, codec, streamCodec);
    }

    public static <T extends ParticleOptions> ParticleType<T> register(
            ResourceLocation location,
            boolean overrideLimiter,
            MapCodec<T> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
    ) {
        return Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                location,
                deserializer(overrideLimiter, codec, streamCodec)
        );
    }

    public static SimpleParticleType simple(boolean overrideLimiter) {
        return new SimpleParticleType(overrideLimiter) {
        };
    }

    public static SimpleParticleType simple() {
        return simple(false);
    }

    public static SimpleParticleType register(ResourceLocation location) {
        return register(location, false);
    }

    public static SimpleParticleType register(ResourceLocation location, boolean overrideLimiter) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, location, simple(overrideLimiter));
    }

    public static SimpleParticleType register(
            ResourceLocation location,
            ParticleFactoryRegistry.PendingParticleFactory<SimpleParticleType> provider
    ) {
        return register(location, false, provider);
    }

    public static SimpleParticleType register(
            ResourceLocation location,
            boolean overrideLimiter,
            ParticleFactoryRegistry.PendingParticleFactory<SimpleParticleType> provider
    ) {
        SimpleParticleType type = Registry.register(BuiltInRegistries.PARTICLE_TYPE, location, simple(overrideLimiter));
        ParticleFactoryRegistry.getInstance().register(type, provider);
        return type;
    }
}
