package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.LifeCycleAPI;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Main.class)
abstract public class MainMixin {
    @ModifyVariable(method = "main", ordinal = 0, at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getSummary()Lnet/minecraft/world/level/storage/LevelSummary;"))
    private static LevelStorageSource.LevelStorageAccess bc_createAccess(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        LifeCycleAPI.newWorldSetup(levelStorageAccess);
        return levelStorageAccess;
    }


    @ModifyArg(method = "method_43613", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getDataTag(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/world/level/DataPackConfig;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/world/level/storage/WorldData;"))
    private static DynamicOps<Tag> bcl_onCreate(DynamicOps<Tag> dynamicOps) {
        if (dynamicOps instanceof RegistryOps<Tag> regOps) {
            LifeCycleAPI.worldCreationStarted(regOps);
        }
        return dynamicOps;
    }

}
