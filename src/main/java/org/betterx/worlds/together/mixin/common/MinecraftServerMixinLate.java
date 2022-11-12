package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.chunkgenerator.ChunkGeneratorUtils;

import com.mojang.datafixers.DataFixer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(value = MinecraftServer.class, priority = 2000)
public class MinecraftServerMixinLate {
    @Shadow
    @Final
    private LayeredRegistryAccess<RegistryLayer> registries;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void bcl_restoreBiomeSource(
            Thread thread,
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            PackRepository packRepository,
            WorldStem worldStem,
            Proxy proxy,
            DataFixer dataFixer,
            Services services,
            ChunkProgressListenerFactory chunkProgressListenerFactory,
            CallbackInfo ci
    ) {
        final Registry<LevelStem> dimensionRegistry = this.registries.compositeAccess()
                                                                     .registryOrThrow(Registries.LEVEL_STEM);
        ChunkGeneratorUtils.restoreOriginalBiomeSourceInAllDimension(dimensionRegistry);
    }
}
