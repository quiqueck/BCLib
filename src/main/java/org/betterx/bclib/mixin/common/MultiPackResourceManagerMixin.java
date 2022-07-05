package org.betterx.bclib.mixin.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MultiPackResourceManager.class)
public class MultiPackResourceManagerMixin {
    private static final String[] BCLIB_MISSING_RESOURCES = new String[]{
            "dimension/the_end.json",
            "dimension/the_nether.json",
            "dimension_type/the_end.json",
            "dimension_type/the_nether.json"
    };

    @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
    private void bclib_hasResource(ResourceLocation resourceLocation, CallbackInfoReturnable<Optional<Resource>> info) {
        if (resourceLocation.getNamespace().equals("minecraft")) {
            for (String key : BCLIB_MISSING_RESOURCES) {
                if (resourceLocation.getPath().equals(key)) {
//                    info.setReturnValue(Optional.empty());
//                    info.cancel();
                    return;
                }
            }
        }
    }
}
