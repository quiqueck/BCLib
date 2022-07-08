package org.betterx.worlds.together.worldPreset.client;


import org.betterx.worlds.together.worldPreset.WorldPreset;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class WorldPresetsClient {
    public static void registerCustomizeUI(ResourceKey<WorldPreset> key, WorldPreset.PresetEditor setupScreen) {
        if (setupScreen != null) {
            PresetEditor.EDITORS.put(Optional.of(key), setupScreen);
        }
    }

    public static void setupClientside() {
    }
}
