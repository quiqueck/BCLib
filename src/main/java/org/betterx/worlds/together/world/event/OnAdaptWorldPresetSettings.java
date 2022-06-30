package org.betterx.worlds.together.world.event;

import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.Optional;

@FunctionalInterface
public interface OnAdaptWorldPresetSettings {
    Optional<Holder<WorldPreset>> adapt(
            Optional<Holder<WorldPreset>> currentPreset,
            WorldGenSettings worldGenSettings
    );
}
