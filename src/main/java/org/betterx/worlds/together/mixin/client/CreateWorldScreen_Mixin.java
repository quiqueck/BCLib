package org.betterx.worlds.together.mixin.client;

import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreen_Mixin {
    @Shadow
    public abstract WorldCreationUiState getUiState();

    @Shadow
    private boolean recreated;


    //this is called when a new world is first created
    @Inject(method = "createNewWorldDirectory", at = @At("RETURN"))
    void wt_createNewWorld(CallbackInfoReturnable<Optional<LevelStorageSource.LevelStorageAccess>> cir) {
        WorldBootstrap.Helpers.onRegistryReady(this.getUiState().getSettings().worldgenLoadContext());
        WorldBootstrap.InGUI.setupNewWorld(cir.getReturnValue());
    }
}
