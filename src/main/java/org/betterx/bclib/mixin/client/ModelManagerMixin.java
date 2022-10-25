package org.betterx.bclib.mixin.client;

import net.minecraft.client.resources.model.ModelManager;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    //TODO: 1.19.3 Disabled for now
//    @Inject(method = "prepare", at = @At("HEAD"))
//    private void bclib_loadCustomModels(
//            ResourceManager resourceManager,
//            ProfilerFiller profilerFiller,
//            CallbackInfoReturnable<ModelBakery> info
//    ) {
//        BCLibClient.modelBakery.loadCustomModels(resourceManager);
//    }
}
