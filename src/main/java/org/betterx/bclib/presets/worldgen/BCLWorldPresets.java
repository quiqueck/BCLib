package org.betterx.bclib.presets.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.BCLBiomeSource;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.api.v2.tag.TagAPI;
import org.betterx.bclib.api.v2.tag.TagType;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

public class BCLWorldPresets {

    public static final TagType.Simple<WorldPreset> WORLD_PRESETS =
            TagAPI.registerType(BuiltinRegistries.WORLD_PRESET, "tags/worldgen/world_preset");
    private static Map<ResourceKey<WorldPreset>, PresetBuilder> BUILDERS = Maps.newHashMap();
    private static final Map<ResourceKey<WorldPreset>, WorldPresetSettings> SETTINGS = Maps.newHashMap();
    public static final ResourceKey<WorldPreset> BCL_WORLD =
            register(
                    BCLib.makeID("normal"),
                    (overworldStem, netherContext, endContext) ->
                            new BCLWorldPresetSettings(BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION).buildPreset(
                                    overworldStem,
                                    netherContext,
                                    endContext
                            ),
                    true
            );
    public static Optional<ResourceKey<WorldPreset>> DEFAULT = Optional.of(BCL_WORLD);
    public static final ResourceKey<WorldPreset> BCL_WORLD_17 = register(
            BCLib.makeID("legacy_17"),
            (overworldStem, netherContext, endContext) ->
                    new BCLWorldPresetSettings(BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE).buildPreset(
                            overworldStem,
                            netherContext,
                            endContext
                    ),
            false
    );

    /**
     * Registers a custom WorldPreset (with custom rules and behaviour)
     * <p>
     * See also {@link org.betterx.bclib.client.presets.WorldPresetsUI} if you need to add a Customize Button/Screen
     * for your preset
     *
     * @param loc The ID of your Preset
     * @return The key you may use to reference your new Preset
     */
    private static ResourceKey<WorldPreset> register(ResourceLocation loc) {
        return register(loc, true);
    }

    private static ResourceKey<WorldPreset> register(ResourceLocation loc, boolean addToNormal) {
        ResourceKey<WorldPreset> key = ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, loc);
        if (addToNormal) {
            WORLD_PRESETS.addUntyped(WorldPresetTags.NORMAL, key.location());
        }

        return key;
    }

    public static void registerPresets() {

    }

    public static ResourceKey<WorldPreset> register(
            ResourceLocation loc,
            PresetBuilder builder,
            boolean visibleInUI
    ) {
        ResourceKey<WorldPreset> key = register(loc, visibleInUI);

        if (BUILDERS == null) {
            BCLib.LOGGER.error("Unable to register WorldPreset '" + loc + "'.");

        } else {
            BUILDERS.put(key, builder);
        }
        return key;
    }

    public static void bootstrapPresets(
            Registry<WorldPreset> presets,
            LevelStem overworldStem,
            LevelGenUtil.Context netherContext,
            LevelGenUtil.Context endContext
    ) {

        for (Map.Entry<ResourceKey<WorldPreset>, PresetBuilder> e : BUILDERS.entrySet()) {
            BCLWorldPreset preset = e.getValue().create(overworldStem, netherContext, endContext);
            SETTINGS.put(e.getKey(), preset.settings);
            BuiltinRegistries.register(presets, e.getKey(), preset);
        }
        BUILDERS = null;
    }

    public static WorldPresetSettings getSettingsForPreset(ResourceKey<WorldPreset> key) {
        return SETTINGS.getOrDefault(key, BCLWorldPresetSettings.DEFAULT);
    }

    @FunctionalInterface
    public interface PresetBuilder {
        BCLWorldPreset create(
                LevelStem overworldStem,
                LevelGenUtil.Context netherContext,
                LevelGenUtil.Context endContext
        );
    }
}
