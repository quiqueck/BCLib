package org.betterx.bclib.interfaces;

import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface ClientLevelAccess {
    @Nullable
    LevelRendererAccess bcl_getLevelRenderer();
    @Nullable
    Particle bcl_addParticle(
            ParticleOptions particleOptions,
            double x, double y, double z,
            double vx, double vy, double vz
    );
}
