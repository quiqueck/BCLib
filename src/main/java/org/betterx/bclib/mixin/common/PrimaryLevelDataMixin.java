package org.betterx.bclib.mixin.common;

import org.betterx.bclib.presets.worldgen.WorldBootstrap;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.PrimaryLevelData;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
    @Shadow
    @Final
    private WorldGenSettings worldGenSettings;
    private static final ThreadLocal<Optional<RegistryOps<Tag>>> bcl_lastRegistryAccess = ThreadLocal.withInitial(
            () -> Optional.empty());

    //This is the way a created (new) world is initializing the PrimaryLevelData
    @ModifyArg(method = "<init>(Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;ZIIIFJJIIIZIZZZLnet/minecraft/world/level/border/WorldBorder$Settings;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/level/timers/TimerQueue;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)V"))
    private static WorldGenSettings bcl_fixOtherSettings(WorldGenSettings worldGenSettings) {
        return WorldBootstrap.enforceInNewWorld(worldGenSettings);
    }

    @Inject(method = "parse", at = @At("HEAD"))
    private static void bcl_parse(
            Dynamic<Tag> dynamic,
            DataFixer dataFixer,
            int i,
            @Nullable CompoundTag compoundTag,
            LevelSettings levelSettings,
            LevelVersion levelVersion,
            WorldGenSettings worldGenSettings,
            Lifecycle lifecycle,
            CallbackInfoReturnable<PrimaryLevelData> cir
    ) {
        if (dynamic.getOps() instanceof RegistryOps<Tag> regOps) {
            bcl_lastRegistryAccess.set(Optional.of(regOps));
        }
    }


    //This is the way an loaded (existing) world is initializing the PrimaryLevelData
    @ModifyArg(method = "parse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;ZIIIFJJIIIZIZZZLnet/minecraft/world/level/border/WorldBorder$Settings;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/level/timers/TimerQueue;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)V"))
    private static WorldGenSettings bcl_fixSettings(WorldGenSettings settings) {
        Optional<RegistryOps<Tag>> registryOps = bcl_lastRegistryAccess.get();
        WorldBootstrap.InGUI.registryReady(registryOps);
        settings = WorldBootstrap.enforceInLoadedWorld(registryOps, settings);
        bcl_lastRegistryAccess.set(Optional.empty());
        return settings;
    }

}
