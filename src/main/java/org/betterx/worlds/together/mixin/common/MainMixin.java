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
    @ModifyVariable(method = "main", ordinal = 0, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getSummary()Lnet/minecraft/world/level/storage/LevelSummary;"))
    private static LevelStorageSource.LevelStorageAccess bc_createAccess(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        WorldBootstrap.DedicatedServer.setupWorld(levelStorageAccess);
        return levelStorageAccess;
    }


    @ModifyArg(method = "method_43613", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getDataTag(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/WorldDataConfiguration;Lnet/minecraft/core/Registry;Lcom/mojang/serialization/Lifecycle;)Lcom/mojang/datafixers/util/Pair;"))
    private static DynamicOps<Tag> bcl_onCreate(DynamicOps<Tag> dynamicOps) {
        if (dynamicOps instanceof RegistryOps<Tag> regOps) {
            WorldBootstrap.DedicatedServer.registryReady(regOps);
        }
        return dynamicOps;
    }

    //TODO: 1.19.3 this may be obsolete, as datapacks are handled differently now
//    @ModifyArg(method = "method_43613", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;<init>(Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/world/level/storage/PrimaryLevelData$SpecialWorldProperty;Lcom/mojang/serialization/Lifecycle;)V"))
//    private static WorldGenSettings bcl_onCreateLevelData(WorldGenSettings worldGenSettings) {
//        WorldBootstrap.DedicatedServer.applyDatapackChangesOnNewWorld(worldGenSettings);
//        return worldGenSettings;
//    }
}
