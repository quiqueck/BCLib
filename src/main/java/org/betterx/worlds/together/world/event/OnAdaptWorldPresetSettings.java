package org.betterx.worlds.together.world.event;

import org.betterx.worlds.together.worldPreset.WorldPreset;

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
