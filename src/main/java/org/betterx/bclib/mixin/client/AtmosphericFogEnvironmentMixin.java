package org.betterx.bclib.mixin.client;

import org.betterx.bclib.client.render.CustomFogRenderer;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogEnvironment.class)
@Environment(EnvType.CLIENT)
public class AtmosphericFogEnvironmentMixin {
    @Inject(
            method = "setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V",
            at = @At("RETURN")
    )
    private void bclib_applyBiomeFog(
            FogData fog,
            Camera camera,
            ClientLevel level,
            float renderDistance,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        CustomFogRenderer.applyAtmosphericFog(fog, camera, level, renderDistance);
    }
}
