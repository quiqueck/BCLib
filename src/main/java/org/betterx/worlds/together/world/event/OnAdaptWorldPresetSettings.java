package org.betterx.worlds.together.world.event;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

@FunctionalInterface
public interface OnAdaptWorldPresetSettings {
    Holder<WorldPreset> adapt(
            Holder<WorldPreset> currentPreset,
            WorldDimensions worldDims
    );
}
