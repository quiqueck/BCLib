package org.betterx.worlds.together.mixin.client;

import org.betterx.worlds.together.world.event.WorldBootstrap;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldDataConfiguration;
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
            WorldDataConfiguration worldDataConfiguration,
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

    //this is called when a new world is first created
    @Inject(method = "createNewWorldDirectory", at = @At("RETURN"))
    void wt_createNewWorld(CallbackInfoReturnable<Optional<LevelStorageSource.LevelStorageAccess>> cir) {
        WorldBootstrap.InGUI.registryReadyOnNewWorld(this.worldGenSettingsComponent);
        WorldBootstrap.InGUI.setupNewWorld(cir.getReturnValue(), this.worldGenSettingsComponent);
    }

    @Inject(method = "onCreate", at = @At("HEAD"))
    void wt_onCreate(CallbackInfo ci) {
        System.out.println("there");
    }
}
