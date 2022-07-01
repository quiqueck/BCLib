package org.betterx.worlds.together.flatLevel;

import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.tags.TagKey;

public class FlatLevelGeneratorPresetTags {
    public static final TagKey<FlatLevelGeneratorPreset> VISIBLE = FlatLevelPresets.FLAT_LEVEL_PRESETS.makeTag(
            WorldsTogether.makeID("visible")
    );
}
