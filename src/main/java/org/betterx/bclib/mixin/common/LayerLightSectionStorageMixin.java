package org.betterx.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LayerLightSectionStorage.class)
public class LayerLightSectionStorageMixin {
    @Shadow
    protected DataLayer getDataLayer(long sectionPos, boolean cached) {
        return null;
    }

    @Inject(method = "getStoredLevel", at = @At(value = "HEAD"), cancellable = true)
    private void bclib_lightFix(long blockPos, CallbackInfoReturnable<Integer> info) {
        try {
            long pos = SectionPos.blockToSection(blockPos);
            DataLayer dataLayer = this.getDataLayer(pos, true);
            info.setReturnValue(dataLayer.get(
                    SectionPos.sectionRelative(BlockPos.getX(blockPos)),
                    SectionPos.sectionRelative(BlockPos.getY(blockPos)),
                    SectionPos.sectionRelative(BlockPos.getZ(blockPos))
            ));
        } catch (Exception e) {
            info.setReturnValue(0);
        }
    }
}
