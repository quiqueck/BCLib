package org.betterx.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public class WorldGenRegionMixin {
    @Final
    @Shadow
    private ChunkAccess center;

    @Final
    @Shadow
    private int writeRadiusCutoff;

    /**
     * Refuses writes outside the region's write radius without vanilla's "Detected setBlock in a far chunk" error
     * (large features may reach past it). Everything else, including the build height check, stays vanilla.
     */
    @Inject(method = "ensureCanWrite", at = @At("HEAD"), cancellable = true)
    private void bcl_silentlyRefuseFarWrites(BlockPos blockPos, CallbackInfoReturnable<Boolean> info) {
        final ChunkPos cPos = center.getPos();
        final int dx = Math.abs(SectionPos.blockToSectionCoord(blockPos.getX()) - cPos.x);
        final int dz = Math.abs(SectionPos.blockToSectionCoord(blockPos.getZ()) - cPos.z);
        if (dx > writeRadiusCutoff || dz > writeRadiusCutoff) {
            info.setReturnValue(false);
        }
    }
}
