package org.betterx.worlds.together.flatLevel;

import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.tag.v3.TagRegistry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;

public class FlatLevelPresets {
    public static TagRegistry.Simple<FlatLevelGeneratorPreset> FLAT_LEVEL_PRESETS =
            TagManager.registerType(
                    Registries.FLAT_LEVEL_GENERATOR_PRESET,
                    "tags/worldgen/flat_level_generator_preset",
                    (b) -> null
            );


    public static ResourceKey<FlatLevelGeneratorPreset> register(ResourceLocation loc) {
        ResourceKey<FlatLevelGeneratorPreset> key = ResourceKey.create(
                Registries.FLAT_LEVEL_GENERATOR_PRESET,
                loc
        );
        FLAT_LEVEL_PRESETS.addUntyped(FlatLevelGeneratorPresetTags.VISIBLE, key.location());
        return key;
    }
}
