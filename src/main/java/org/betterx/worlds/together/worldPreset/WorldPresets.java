package org.betterx.worlds.together.worldPreset;

import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.tag.v3.TagRegistry;
import org.betterx.worlds.together.world.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.client.WorldPresetsClient;
import org.betterx.worlds.together.worldPreset.settings.VanillaWorldPresetSettings;
import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

public class WorldPresets {

    public static final TagRegistry.Simple<WorldPreset> WORLD_PRESETS =
            TagManager.registerType(BuiltinRegistries.WORLD_PRESET, "tags/worldgen/world_preset");
    private static Map<ResourceKey<WorldPreset>, PresetBuilder> BUILDERS = Maps.newHashMap();
    private static final Map<ResourceKey<WorldPreset>, WorldPresetSettings> SETTINGS = Maps.newHashMap();
    public static Optional<ResourceKey<WorldPreset>> DEFAULT = Optional.of(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);

    public static Holder<WorldPreset> get(RegistryAccess access, ResourceKey<WorldPreset> key) {
        return ((access != null) ? access : BuiltinRegistries.ACCESS)
                .registryOrThrow(Registry.WORLD_PRESET_REGISTRY)
                .getHolderOrThrow(key);
    }

    /**
     * Registers a custom WorldPreset (with custom rules and behaviour)
     * <p>
     * See also {@link WorldPresetsClient} if you need to add a Customize Button/Screen
     * for your preset
     *
     * @param loc         The ID of your Preset
     * @param visibleInUI if true, the preset will show up in the UI on world creataion
     * @return The key you may use to reference your new Preset
     */
    private static ResourceKey<WorldPreset> register(ResourceLocation loc, boolean visibleInUI) {
        ResourceKey<WorldPreset> key = ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, loc);
        if (visibleInUI) {
            WORLD_PRESETS.addUntyped(WorldPresetTags.NORMAL, key.location());
        }

        return key;
    }

    public static void ensureStaticallyLoaded() {

    }

    public static ResourceKey<WorldPreset> register(
            ResourceLocation loc,
            PresetBuilder builder,
            boolean visibleInUI
    ) {
        ResourceKey<WorldPreset> key = register(loc, visibleInUI);

        if (BUILDERS == null) {
            WorldsTogether.LOGGER.error("Unable to register WorldPreset '" + loc + "'.");

        } else {
            BUILDERS.put(key, builder);
        }
        return key;
    }

    public static void bootstrapPresets(
            Registry<WorldPreset> presets,
            LevelStem overworldStem,
            WorldGenUtil.Context netherContext,
            WorldGenUtil.Context endContext
    ) {
        PresetsRegistry.onLoad();

        for (Map.Entry<ResourceKey<WorldPreset>, PresetBuilder> e : BUILDERS.entrySet()) {
            TogetherWorldPreset preset = e.getValue().create(overworldStem, netherContext, endContext);
            SETTINGS.put(e.getKey(), preset.settings);
            BuiltinRegistries.register(presets, e.getKey(), preset);
        }
        BUILDERS = null;
    }

    public static WorldPresetSettings getSettingsForPreset(ResourceKey<WorldPreset> key) {
        return SETTINGS.getOrDefault(key, VanillaWorldPresetSettings.DEFAULT);
    }

    @FunctionalInterface
    public interface PresetBuilder {
        TogetherWorldPreset create(
                LevelStem overworldStem,
                WorldGenUtil.Context netherContext,
                WorldGenUtil.Context endContext
        );
    }
}
