package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.ClientLevelAccess;
import org.betterx.bclib.interfaces.LevelRendererAccess;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.jetbrains.annotations.Nullable;

@Mixin(ClientLevel.class)
@Environment(EnvType.CLIENT)
public class ClientLevelMixin implements ClientLevelAccess {
    @Shadow
    @Final
    private LevelRenderer levelRenderer;

    @Override
    @Nullable
    public LevelRendererAccess bcl_getLevelRenderer() {
        if (this.levelRenderer instanceof LevelRendererAccess a) {
            return a;
        }
        return null;
    }

    @Override
    @Nullable
    public Particle bcl_addParticle(
            ParticleOptions particleOptions,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz
    ) {
        var renderer = this.bcl_getLevelRenderer();
        if (renderer != null) {
            return renderer.bcl_addParticle(particleOptions, x, y, z, vx, vy, vz);
        }
        return null;
    }
}
