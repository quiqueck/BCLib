package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.world.event.WorldBootstrap;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
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


    @ModifyArg(method = "method_43613", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getDataTag(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/WorldDataConfiguration;Lnet/minecraft/core/Registry;Lcom/mojang/serialization/Lifecycle;)Lcom/mojang/datafixers/util/Pair;"))
    private static DynamicOps<Tag> bcl_onCreate(DynamicOps<Tag> dynamicOps) {
        if (dynamicOps instanceof RegistryOps<Tag> regOps) {
            WorldBootstrap.DedicatedServer.registryReady(regOps);
        }
        WorldBootstrap.DedicatedServer.setupWorld(bcl_levelStorageAccess);
        return dynamicOps;
    }
}
