package org.betterx.worlds.together.world.event;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class AdaptWorldPresetSettingEvent extends EventImpl<OnAdaptWorldPresetSettings> {
    public Holder<WorldPreset> emit(Holder<WorldPreset> start, WorldDimensions worldDims) {
        for (OnAdaptWorldPresetSettings a : handlers) {
            start = a.adapt(start, worldDims);
        }
        return start;
    }
}
