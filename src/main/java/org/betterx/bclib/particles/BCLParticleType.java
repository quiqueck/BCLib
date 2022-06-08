package org.betterx.bclib.particles;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class BCLParticleType {
    public static SimpleParticleType simple(boolean alwaysSpawn) {
        return new SimpleParticleType(alwaysSpawn) {
        };
    }

    public static SimpleParticleType simple() {
        return simple(false);
    }

    public static SimpleParticleType registerSimple(ResourceLocation location) {
        return registerSimple(location, false);
    }

    public static SimpleParticleType registerSimple(ResourceLocation location, boolean alwaysSpawn) {
        return Registry.register(Registry.PARTICLE_TYPE, location, simple(alwaysSpawn));
    }

    public static SimpleParticleType registerSimple(ResourceLocation location,
                                                    ParticleFactoryRegistry.PendingParticleFactory<SimpleParticleType> provider) {
        return registerSimple(location, false, provider);
    }

    public static SimpleParticleType registerSimple(ResourceLocation location,
                                                    boolean alwaysSpawn,
                                                    ParticleFactoryRegistry.PendingParticleFactory<SimpleParticleType> provider) {
        SimpleParticleType type = Registry.register(Registry.PARTICLE_TYPE, location, simple(alwaysSpawn));
        ParticleFactoryRegistry.getInstance().register(type, provider);
        return type;
    }
}
