package org.betterx.bclib.mixin.client;

import org.betterx.worlds.together.worldPreset.client.WorldPresetsClient;

import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUIStateMixin {
    @Shadow
    public abstract WorldCreationUiState.WorldTypeEntry getWorldType();

    @Inject(method = "getPresetEditor", at = @At("HEAD"), cancellable = true)
    private void bclib_getPresetEditor(CallbackInfoReturnable<PresetEditor> cir) {
        final PresetEditor editor = WorldPresetsClient.getSetupScreenForPreset(this.getWorldType().preset());
        if (editor != null) cir.setReturnValue(editor);
    }
}
