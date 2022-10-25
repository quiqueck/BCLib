package org.betterx.worlds.together.mixin.client;

import org.betterx.worlds.together.world.event.WorldBootstrap;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.WorldLoader;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
    @Shadow
    @Final
    public WorldGenSettingsComponent worldGenSettingsComponent;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void wt_init(
            Screen screen,
            DataPackConfig dataPackConfig,
            WorldGenSettingsComponent worldGenSettingsComponent,
            CallbackInfo ci
    ) {
        WorldBootstrap.InGUI.registryReadyOnNewWorld(worldGenSettingsComponent);
    }

    //Change the WorldPreset that is selected by default on the Create World Screen
    @ModifyArg(method = "openFresh", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldGenSettingsComponent;<init>(Lnet/minecraft/client/gui/screens/worldselection/WorldCreationContext;Ljava/util/Optional;Ljava/util/OptionalLong;)V"))
    private static Optional<ResourceKey<WorldPreset>> wt_NewDefault(Optional<ResourceKey<WorldPreset>> preset) {
        return Optional.of(WorldPresets.getDEFAULT());
    }
//
//    @Redirect(method = "method_41854", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/RegistryAccess$Writable;freeze()Lnet/minecraft/core/RegistryAccess$Frozen;"))
//    private static RegistryAccess.Frozen loadDynamicRegistry(
//            RegistryAccess.Writable mutableRegistryManager,
//            ResourceManager dataPackManager
//    ) {
//        // This loads the dynamic registry from the data pack
//        RegistryOps.createAndLoad(JsonOps.INSTANCE, mutableRegistryManager, dataPackManager);
//        return mutableRegistryManager.freeze();
//    }

    //Make sure the WorldGenSettings used to populate the create screen match the default WorldPreset
    @ModifyArg(method = "openFresh", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/WorldLoader;load(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static WorldLoader.WorldDataSupplier<WorldGenSettings> wt_NewDefaultSettings(WorldLoader.WorldDataSupplier<WorldGenSettings> worldDataSupplier) {
        return worldDataSupplier;
        //TODO: 1.19.3 New DataProviders might handle this edge case automatically?
//        return (resourceManager, dataPackConfig) -> {
////            Pair<WorldGenSettings, RegistryAccess.Frozen> res = worldDataSupplier.get(resourceManager, dataPackConfig);
////            WorldGenSettings defaultGen = net.minecraft.world.level.levelgen.presets.WorldPresets.createNormalWorldFromPreset(frozen);
////            WorldBootstrap.InGUI.setDefaultCreateWorldSettings(defaultGen);
//            RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
//
//
//            WorldGenUtil.preloadWorldPresets(resourceManager, writable);
//            RegistryOps<JsonElement> registryOps = RegistryOps.createAndLoad(
//                    JsonOps.INSTANCE, writable, resourceManager
//            );
//            RegistryAccess.Frozen frozen = writable.freeze();
//            WorldBootstrap.InGUI.registryReady(frozen);
//
//
//            return WorldGenUtil.defaultWorldDataSupplier(registryOps, frozen);
//        };
    }

    //this is called when a new world is first created
    @Inject(method = "createNewWorldDirectory", at = @At("RETURN"))
    void wt_createNewWorld(CallbackInfoReturnable<Optional<LevelStorageSource.LevelStorageAccess>> cir) {
        WorldBootstrap.InGUI.registryReadyOnNewWorld(this.worldGenSettingsComponent);
        WorldBootstrap.InGUI.setupNewWorld(cir.getReturnValue(), this.worldGenSettingsComponent);
    }
}
