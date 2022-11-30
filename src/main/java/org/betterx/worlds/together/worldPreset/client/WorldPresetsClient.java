package org.betterx.worlds.together.worldPreset.client;

import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class WorldPresetsClient {
    public static void registerCustomizeUI(ResourceKey<WorldPreset> key, PresetEditor setupScreen) {
        if (setupScreen != null) {
            //TODO: 1.19.3 this is called out of order
            PresetEditor.EDITORS.put(Optional.of(key), setupScreen);
        }
    }

    public static void setupClientside() {
    }
}
