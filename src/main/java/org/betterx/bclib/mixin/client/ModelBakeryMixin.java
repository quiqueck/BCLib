package org.betterx.bclib.mixin.client;

import org.betterx.bclib.api.v2.ModIntegrationAPI;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    @Final
    @Shadow
    private Map<ResourceLocation, UnbakedModel> unbakedCache;

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void bclib_findEmissiveModels(
            BlockColors blockColors, ProfilerFiller profilerFiller, Map map, Map map2, CallbackInfo ci
    ) {
        //CustomModelBakery.setModelsLoaded(false);
        if (ModIntegrationAPI.hasCanvas()) {
            //TODO:1.19.3 this needs to change
            //CustomModelBakery.loadEmissiveModels(unbakedCache);
        }
    }
}
