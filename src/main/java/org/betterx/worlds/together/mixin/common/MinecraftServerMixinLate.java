package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.chunkgenerator.ChunkGeneratorUtils;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(value = MinecraftServer.class, priority = 2000)
public class MinecraftServerMixinLate {
    @Inject(at = @At("RETURN"), method = "<init>")
    private void bcl_restoreBiomeSource(
            Thread thread,
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            PackRepository packRepository,
            WorldStem worldStem,
            Proxy proxy,
            DataFixer dataFixer,
            MinecraftSessionService minecraftSessionService,
            GameProfileRepository gameProfileRepository,
            GameProfileCache gameProfileCache,
            ChunkProgressListenerFactory chunkProgressListenerFactory,
            CallbackInfo ci
    ) {
        ChunkGeneratorUtils.restoreOriginalBiomeSourceInAllDimension(worldStem.worldData().worldGenSettings());
    }
}
