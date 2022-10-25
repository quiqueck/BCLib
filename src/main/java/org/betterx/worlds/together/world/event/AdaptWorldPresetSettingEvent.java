package org.betterx.worlds.together.world.event;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Optional;

public class AdaptWorldPresetSettingEvent extends EventImpl<OnAdaptWorldPresetSettings> {
    public Optional<Holder<WorldPreset>> emit(Optional<Holder<WorldPreset>> start, WorldDimensions worldDims) {
        for (OnAdaptWorldPresetSettings a : handlers) {
            start = a.adapt(start, worldDims);
        }
        return start;
    }
}
