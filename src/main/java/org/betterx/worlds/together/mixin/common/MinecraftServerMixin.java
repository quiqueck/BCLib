package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.storage.WorldData;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * We need a hook here to alter surface rules after Fabric did add its biomes
 * in {@link net.fabricmc.fabric.mixin.biome.MixinMinecraftServer}
 */
@Mixin(value = MinecraftServer.class, priority = 2000)
public class MinecraftServerMixin {
    @Shadow
    @Final
    private RegistryAccess.Frozen registryHolder;
    @Shadow
    @Final
    protected WorldData worldData;

    @Inject(method = "createLevels", at = @At(value = "HEAD"))
    private void together_addSurfaceRules(ChunkProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        WorldBootstrap.finalizeWorldGenSettings(this.worldData.worldGenSettings());

    }
}
