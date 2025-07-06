package org.betterx.bclib.mixin.client.boat;


import org.betterx.bclib.items.boat.BoatTypeOverride;

import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.vehicle.Boat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatRenderer.class)
public abstract class BoatRendererMixin extends EntityRenderer<Boat> {
    protected BoatRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bcl_init(EntityRendererProvider.Context context, boolean bl, CallbackInfo ci) {
        BoatTypeOverride.values().forEach(type -> type.createBoatModels(context));
    }
}
