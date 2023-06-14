package org.betterx.worlds.together.mixin.client;

import org.betterx.worlds.together.world.event.WorldBootstrap;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.OptionalLong;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreen_Mixin {
    @Shadow
    public abstract WorldCreationUiState getUiState();

    @Shadow
    private boolean recreated;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void wt_init(
            Minecraft minecraft,
            Screen screen,
            WorldCreationContext worldCreationContext,
            Optional optional,
            OptionalLong optionalLong,
            CallbackInfo ci
    ) {
        //WorldBootstrap.InGUI.registryReadyOnNewWorld(worldGenSettingsComponent);
    }

    //Change the WorldPreset that is selected by default on the Create World Screen
    @ModifyArg(method = "openFresh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/screens/worldselection/WorldCreationContext;Ljava/util/Optional;Ljava/util/OptionalLong;)V"))
    private static Optional<ResourceKey<WorldPreset>> wt_NewDefault(Optional<ResourceKey<WorldPreset>> preset) {
        return Optional.of(WorldPresets.getDEFAULT());
    }

    //this is called when a new world is first created
    @Inject(method = "createNewWorldDirectory", at = @At("RETURN"))
    void wt_createNewWorld(CallbackInfoReturnable<Optional<LevelStorageSource.LevelStorageAccess>> cir) {
        WorldBootstrap.InGUI.registryReadyOnNewWorld(this.getUiState().getSettings());
        WorldBootstrap.InGUI.setupNewWorld(cir.getReturnValue(), this.getUiState(), this.recreated);
    }
}
