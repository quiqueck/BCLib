package org.betterx.bclib.presets;

import org.betterx.worlds.together.tag.v3.TagRegistry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;

/**
 * @deprecated Use {@link org.betterx.worlds.together.flatLevel.FlatLevelPresets} instead
 */
@Deprecated(forRemoval = true)
public class FlatLevelPresets {
    /**
     * @deprecated Use {@link org.betterx.worlds.together.flatLevel.FlatLevelPresets#FLAT_LEVEL_PRESETS} instead
     */
    @Deprecated(forRemoval = true)
    public static TagRegistry.Simple<FlatLevelGeneratorPreset> FLAT_LEVEL_PRESETS = org.betterx.worlds.together.flatLevel.FlatLevelPresets.FLAT_LEVEL_PRESETS;


    /**
     * @deprecated Use {@link org.betterx.worlds.together.flatLevel.FlatLevelPresets#register(ResourceLocation)} instead
     */
    @Deprecated(forRemoval = true)
    public static ResourceKey<FlatLevelGeneratorPreset> register(ResourceLocation loc) {
        return org.betterx.worlds.together.flatLevel.FlatLevelPresets.register(loc);
    }
}
