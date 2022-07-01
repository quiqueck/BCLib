package org.betterx.worlds.together.world.event;

import org.betterx.worlds.together.worldPreset.WorldPreset;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.Optional;

public class AdaptWorldPresetSettingEvent extends EventImpl<OnAdaptWorldPresetSettings> {
    public Optional<Holder<WorldPreset>> emit(Optional<Holder<WorldPreset>> start, WorldGenSettings settings) {
        for (OnAdaptWorldPresetSettings a : handlers) {
            start = a.adapt(start, settings);
        }
        return start;
    }
}
