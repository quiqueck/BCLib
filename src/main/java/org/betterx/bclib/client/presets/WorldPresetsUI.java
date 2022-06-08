package org.betterx.bclib.client.presets;

import org.betterx.bclib.client.gui.screens.WorldSetupScreen;
import org.betterx.bclib.presets.worldgen.BCLWorldPresets;

import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class WorldPresetsUI {
    public static void registerCustomizeUI(ResourceKey<WorldPreset> key, PresetEditor setupScreen) {
        if (setupScreen != null) {
            PresetEditor.EDITORS.put(Optional.of(key), setupScreen);
        }
    }

    public static void setupClientside() {
        registerCustomizeUI(BCLWorldPresets.BCL_WORLD, (createWorldScreen, worldCreationContext) -> {
            return new WorldSetupScreen(createWorldScreen, worldCreationContext);
        });
    }
}
