package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = Main.class, priority = 200)
abstract public class MainMixin {
    private static LevelStorageSource.LevelStorageAccess bcl_levelStorageAccess = null;

    @ModifyVariable(method = "main", ordinal = 0, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getSummary()Lnet/minecraft/world/level/storage/LevelSummary;"))
    private static LevelStorageSource.LevelStorageAccess bc_createAccess(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        bcl_levelStorageAccess = levelStorageAccess;
        WorldBootstrap.DedicatedServer.applyWorldPatches(levelStorageAccess);
        return levelStorageAccess;
    }
    
    @ModifyArg(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;)V"))
    private static RegistryAccess bcl_onCreate(RegistryAccess registryAccess) {
        WorldBootstrap.DedicatedServer.registryReady(registryAccess);
        WorldBootstrap.DedicatedServer.setupWorld(bcl_levelStorageAccess);

        return registryAccess;
    }
}
