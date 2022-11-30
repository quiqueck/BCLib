package org.betterx.worlds.together.worldPreset;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.entrypoints.EntrypointUtil;
import org.betterx.worlds.together.entrypoints.WorldPresetBootstrap;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.tag.v3.TagRegistry;
import org.betterx.worlds.together.worldPreset.client.WorldPresetsClient;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import com.google.common.collect.Maps;

import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

public class WorldPresets {
    @FunctionalInterface
    public interface OverworldBuilder {
        LevelStem make(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> noiseGeneratorSettings);
    }

    public static final TagRegistry.UnTyped<WorldPreset> WORLD_PRESETS =
            TagManager.registerType(Registries.WORLD_PRESET, "tags/worldgen/world_preset");
    private static Map<ResourceKey<WorldPreset>, PresetBuilder> BUILDERS = Maps.newHashMap();
    private static ResourceKey<WorldPreset> DEFAULT = net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL;

    public static Holder<WorldPreset> get(RegistryAccess access, ResourceKey<WorldPreset> key) {
        return access
                .registryOrThrow(Registries.WORLD_PRESET)
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
    private static ResourceKey<WorldPreset> register(ResourceKey<WorldPreset> key, boolean visibleInUI) {
        //ResourceKey<WorldPreset> key = ResourceKey.create(Registries.WORLD_PRESET, loc);
        if (visibleInUI) {
            if (!didExplicitlySetDefault && DEFAULT == net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL) {
                DEFAULT = key;
            }
            WORLD_PRESETS.addUntyped(WorldPresetTags.NORMAL, key.location());
        }

        return key;
    }

    public static void ensureStaticallyLoaded() {

    }

    public static ResourceKey<WorldPreset> createKey(ResourceLocation loc) {
        return ResourceKey.create(Registries.WORLD_PRESET, loc);
    }

    public static ResourceKey<WorldPreset> register(
            ResourceKey<WorldPreset> loc,
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

    @ApiStatus.Internal
    public static void bootstrapPresets(
            BootstapContext<WorldPreset> bootstrapContext,
            LevelStem overworldStem,
            WorldGenUtil.Context netherContext,
            WorldGenUtil.Context endContext,
            HolderGetter<NoiseGeneratorSettings> noiseSettings,
            OverworldBuilder noiseBasedOverworld
    ) {
        EntrypointUtil.getCommon(WorldPresetBootstrap.class)
                      .forEach(e -> e.bootstrapWorldPresets());

        for (Map.Entry<ResourceKey<WorldPreset>, PresetBuilder> e : BUILDERS.entrySet()) {
            TogetherWorldPreset preset = e.getValue()
                                          .create(
                                                  overworldStem, netherContext, endContext,
                                                  noiseSettings, noiseBasedOverworld
                                          );
            bootstrapContext.register(e.getKey(), preset);
        }
        BUILDERS = null;
    }

    public static ResourceKey<WorldPreset> getDEFAULT() {
        return DEFAULT;
    }

    private static boolean didExplicitlySetDefault = false;

    @ApiStatus.Internal
    public static void setDEFAULT(ResourceKey<WorldPreset> DEFAULT) {
        didExplicitlySetDefault = true;
        WorldPresets.DEFAULT = DEFAULT;
    }


    @FunctionalInterface
    public interface PresetBuilder {
        TogetherWorldPreset create(
                LevelStem overworldStem,
                WorldGenUtil.Context netherContext,
                WorldGenUtil.Context endContext,
                HolderGetter<NoiseGeneratorSettings> noiseSettings,
                OverworldBuilder noiseBasedOverworld
        );
    }
}
