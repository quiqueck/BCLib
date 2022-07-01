package org.betterx.worlds.together.flatLevel;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.tag.v3.TagRegistry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class FlatLevelPresets {
    static final ResourceKey<Registry<FlatLevelGeneratorPreset>> FLAT_LEVEL_GENERATOR_PRESET_REGISTRY
            = ResourceKey.createRegistryKey(WorldsTogether.makeID("worldgen/flat_level_generator_preset"));

    public static final Registry<FlatLevelGeneratorPreset> FLAT_LEVEL_GENERATOR_PRESET
            = Registry.registerSimple(FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, (registry) -> null);

    public static TagRegistry.Simple<FlatLevelGeneratorPreset> FLAT_LEVEL_PRESETS =
            TagManager.registerType(
                    FLAT_LEVEL_GENERATOR_PRESET_REGISTRY,
                    "tags/worldgen/flat_level_generator_preset",
                    (b) -> null
            );


    public static ResourceKey<FlatLevelGeneratorPreset> register(ResourceLocation loc) {
        ResourceKey<FlatLevelGeneratorPreset> key = ResourceKey.create(
                FLAT_LEVEL_GENERATOR_PRESET_REGISTRY,
                loc
        );
        FLAT_LEVEL_PRESETS.addUntyped(FlatLevelGeneratorPresetTags.VISIBLE, key.location());
        return key;
    }
}
