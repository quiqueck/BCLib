package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.LevelRendererAccess;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.jetbrains.annotations.Nullable;

@Mixin(LevelRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class LevelRendererMixin implements LevelRendererAccess {
    public Particle bcl_addParticle(
            ParticleOptions particleOptions,
            double x, double y, double z,
            double vx, double vy, double vz
    ) {
        return this.addParticleInternal(particleOptions, false, x, y, z, vx, vy, vz);
    }


    @Shadow
    @Nullable
    abstract Particle addParticleInternal(
            ParticleOptions particleOptions,
            boolean bl,
            double d,
            double e,
            double f,
            double g,
            double h,
            double i
    );
}
