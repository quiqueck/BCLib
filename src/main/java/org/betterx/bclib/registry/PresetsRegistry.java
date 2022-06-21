package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.BCLBiomeSource;
import org.betterx.bclib.presets.worldgen.BCLWorldPresetSettings;
import org.betterx.worlds.together.worldPreset.WorldPresets;
import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Optional;

public class PresetsRegistry {
    public static ResourceKey<WorldPreset> BCL_WORLD;
    public static ResourceKey<WorldPreset> BCL_WORLD_17;

    public static void onLoad() {
        BCL_WORLD =
                WorldPresets.register(
                        BCLib.makeID("normal"),
                        (overworldStem, netherContext, endContext) ->
                                new BCLWorldPresetSettings(BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION).buildPreset(
                                        overworldStem,
                                        netherContext,
                                        endContext
                                ),
                        true
                );

        BCL_WORLD_17 = WorldPresets.register(
                BCLib.makeID("legacy_17"),
                (overworldStem, netherContext, endContext) ->
                        new BCLWorldPresetSettings(BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE).buildPreset(
                                overworldStem,
                                netherContext,
                                endContext
                        ),
                false
        );

        WorldPresetSettings.DEFAULT = BCLWorldPresetSettings.DEFAULT;
        WorldPresets.DEFAULT = Optional.of(BCL_WORLD);

        WorldPresetSettings.register(BCLib.makeID("bcl_world_preset_settings"), BCLWorldPresetSettings.CODEC);
    }

}
