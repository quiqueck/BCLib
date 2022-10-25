package org.betterx.worlds.together.mixin.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * We need a hook here to alter surface rules after Fabric did add its biomes
 * in {@link net.fabricmc.fabric.mixin.biome.MinecraftServerMixin}
 */
@Mixin(value = MinecraftServer.class, priority = 2000)
public class MinecraftServerMixin {
//    @Shadow
//    @Final
//    private RegistryAccess.Frozen registryHolder;
//    @Shadow
//    @Final
//    protected WorldData worldData;

    @Inject(method = "createLevels", at = @At(value = "HEAD"))
    private void together_addSurfaceRules(ChunkProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        //TODO: 1.19.3 Dimensions are handled differently now
        //WorldBootstrap.finalizeWorldGenSettings(this.worldData.worldGenSettings());

    }
}
