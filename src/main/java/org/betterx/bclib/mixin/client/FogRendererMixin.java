package org.betterx.bclib.mixin.client;

import org.betterx.bclib.client.render.CustomFogRenderer;
import org.betterx.bclib.util.BackgroundInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At("RETURN"))
    private static void bclib_onRender(
            Camera camera,
            float tickDelta,
            ClientLevel world,
            int i,
            float f,
            CallbackInfo info
    ) {
        FogType fogType = camera.getFluidInCamera();
        if (fogType != FogType.WATER && world.dimension().equals(Level.END)) {
            Entity entity = camera.getEntity();
            boolean skip = false;
            if (entity instanceof LivingEntity) {
                MobEffectInstance effect = ((LivingEntity) entity).getEffect(MobEffects.NIGHT_VISION);
                skip = effect != null && effect.getDuration() > 0;
            }
            if (!skip) {
                fogRed *= 4;
                fogGreen *= 4;
                fogBlue *= 4;
            }
        }

        BackgroundInfo.fogColorRed = fogRed;
        BackgroundInfo.fogColorGreen = fogGreen;
        BackgroundInfo.fogColorBlue = fogBlue;
    }

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void bclib_fogDensity(
            Camera camera,
            FogRenderer.FogMode fogMode,
            float viewDistance,
            boolean thickFog,
            float g,
            CallbackInfo ci
    ) {
        if (CustomFogRenderer.applyFogDensity(camera, viewDistance, thickFog)) {
            ci.cancel();
        }
    }
}
