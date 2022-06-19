package org.betterx.bclib.mixin.client;

import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.presets.worldgen.BCLWorldPresets;
import org.betterx.bclib.presets.worldgen.WorldBootstrap;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.core.RegistryAccess;
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
    private void bcl_init(
            Screen screen,
            DataPackConfig dataPackConfig,
            WorldGenSettingsComponent worldGenSettingsComponent,
            CallbackInfo ci
    ) {
        InternalBiomeAPI.initRegistry(worldGenSettingsComponent.registryHolder());
    }

    //Change the WorldPreset that is selected by default on the Create World Screen
    @ModifyArg(method = "openFresh", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldGenSettingsComponent;<init>(Lnet/minecraft/client/gui/screens/worldselection/WorldCreationContext;Ljava/util/Optional;Ljava/util/OptionalLong;)V"))
    private static Optional<ResourceKey<WorldPreset>> bcl_NewDefault(Optional<ResourceKey<WorldPreset>> preset) {
        return BCLWorldPresets.DEFAULT;
    }

    //Make sure the WorldGenSettings used to populate the create screen match the default WorldPreset
    @ModifyArg(method = "openFresh", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/WorldLoader;load(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static WorldLoader.WorldDataSupplier<WorldGenSettings> bcl_NewDefaultSettings(WorldLoader.WorldDataSupplier<WorldGenSettings> worldDataSupplier) {
        return (resourceManager, dataPackConfig) -> {
            Pair<WorldGenSettings, RegistryAccess.Frozen> res = worldDataSupplier.get(resourceManager, dataPackConfig);
            return LevelGenUtil.defaultWorldDataSupplier(res.getSecond());
        };
    }

    //this is called when a new world is first created
    @Inject(method = "createNewWorldDirectory", at = @At("RETURN"))
    void bcl_createNewWorld(CallbackInfoReturnable<Optional<LevelStorageSource.LevelStorageAccess>> cir) {
        WorldBootstrap.InGUI.registryReady(this.worldGenSettingsComponent);
        WorldBootstrap.InGUI.setupNewWorld(cir.getReturnValue(), this.worldGenSettingsComponent);
    }
}
