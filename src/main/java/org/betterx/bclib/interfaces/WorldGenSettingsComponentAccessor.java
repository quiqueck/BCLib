package org.betterx.bclib.interfaces;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Optional;

public interface WorldGenSettingsComponentAccessor {
    Optional<Holder<WorldPreset>> bcl_getPreset();
}
