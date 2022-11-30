package org.betterx.bclib.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class BCLParticleType {

    public static <T extends ParticleOptions> ParticleType<T> deserializer(
            ParticleOptions.Deserializer<T> factory,
            Codec<T> codec
    ) {
        return deserializer(false, factory, codec);
    }

    public static <T extends ParticleOptions> ParticleType<T> deserializer(
            boolean overrideLimiter,
            ParticleOptions.Deserializer<T> factory,
            Codec<T> codec
    ) {
        return new ParticleType<T>(overrideLimiter, factory) {
            @Override
            public Codec<T> codec() {
                return codec;
            }
        };
    }

    public static <T extends ParticleOptions> ParticleType<T> register(
            ResourceLocation location,
            ParticleOptions.Deserializer<T> factory,
            Codec<T> codec
    ) {
        return register(location, false, factory, codec);
    }

    public static <T extends ParticleOptions> ParticleType<T> register(
            ResourceLocation location,
            boolean overrideLimiter,
            ParticleOptions.Deserializer<T> factory,
            Codec<T> codec
    ) {
        return Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                location,
                deserializer(overrideLimiter, factory, codec)
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
