package org.betterx.worlds.together.world.event;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Optional;

@FunctionalInterface
public interface OnAdaptWorldPresetSettings {
    Optional<Holder<WorldPreset>> adapt(
            Optional<Holder<WorldPreset>> currentPreset,
            WorldDimensions worldDims
    );
}
