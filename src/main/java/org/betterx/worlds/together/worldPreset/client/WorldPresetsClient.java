package org.betterx.worlds.together.worldPreset.client;

import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;

import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class WorldPresetsClient {
    private static final Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = new HashMap<>();

    public static void registerCustomizeUI(ResourceKey<WorldPreset> key, PresetEditor setupScreen) {
        if (setupScreen != null) {
            EDITORS.put(Optional.of(key), setupScreen);
        }
    }

    public static PresetEditor getSetupScreenForPreset(Holder<WorldPreset> holder) {
        if (holder != null) {
            PresetEditor editor = EDITORS.get(holder.unwrapKey());

            if (editor == null
                    && holder.isBound()
                    && holder.value() instanceof TogetherWorldPreset preset
                    && preset.parentKey != null
            ) {
                editor = EDITORS.get(Optional.of(preset.parentKey));
            }

            return editor;
        }
        return null;
    }

    public static void setupClientside() {
    }
}
