package org.betterx.worlds.together.mixin.common;

import net.minecraft.world.level.storage.PrimaryLevelData;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
    //TODO: 1.19.3 This was changed completley
//    @Shadow
//    @Final
//    private WorldGenSettings worldGenSettings;
//    private static final ThreadLocal<Optional<RegistryOps<Tag>>> bcl_lastRegistryAccess = ThreadLocal.withInitial(
//            () -> Optional.empty());
//
//    //This is the way a created (new) world is initializing the PrimaryLevelData
//    @ModifyArg(method = "<init>(Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;ZIIIFJJIIIZIZZZLnet/minecraft/world/level/border/WorldBorder$Settings;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/level/timers/TimerQueue;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)V"))
//    private static WorldGenSettings bcl_fixOtherSettings(WorldGenSettings worldGenSettings) {
//        return WorldBootstrap.enforceInNewWorld(worldGenSettings);
//    }
//
//    @Inject(method = "parse", at = @At("HEAD"))
//    private static void bcl_parse(
//            Dynamic<Tag> dynamic,
//            DataFixer dataFixer,
//            int i,
//            @Nullable CompoundTag compoundTag,
//            LevelSettings levelSettings,
//            LevelVersion levelVersion,
//            WorldGenSettings worldGenSettings,
//            Lifecycle lifecycle,
//            CallbackInfoReturnable<PrimaryLevelData> cir
//    ) {
//        if (dynamic.getOps() instanceof RegistryOps<Tag> regOps) {
//            bcl_lastRegistryAccess.set(Optional.of(regOps));
//        }
//    }
//
//
//    //This is the way a loaded (existing) world is initializing the PrimaryLevelData
//    @ModifyArg(method = "parse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;ZIIIFJJIIIZIZZZLnet/minecraft/world/level/border/WorldBorder$Settings;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/level/timers/TimerQueue;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)V"))
//    private static WorldGenSettings bcl_fixSettings(WorldGenSettings settings) {
//        Optional<RegistryOps<Tag>> registryOps = bcl_lastRegistryAccess.get();
//        WorldBootstrap.InGUI.registryReadyOnLoadedWorld(registryOps);
//        settings = WorldBootstrap.enforceInLoadedWorld(registryOps, settings);
//        bcl_lastRegistryAccess.set(Optional.empty());
//        return settings;
//    }

}
