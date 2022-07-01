package org.betterx.worlds.together.worldPreset;

import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.tag.v3.TagRegistry;
import org.betterx.worlds.together.worldPreset.client.WorldPresetsClient;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;

import com.google.common.collect.Maps;

import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

public class WorldPresets {
    public static final ResourceKey<Registry<WorldPreset>> WORLD_PRESET_REGISTRY
            = ResourceKey.createRegistryKey(WorldsTogether.makeID("worldgen/world_preset"));

    public static final Registry<WorldPreset> WORLD_PRESET
            = Registry.registerSimple(WORLD_PRESET_REGISTRY, (registry) -> null);

    public static final TagRegistry.Simple<WorldPreset> WORLD_PRESETS =
            TagManager.registerType(WORLD_PRESET, "tags/worldgen/world_preset");


    private static Map<ResourceKey<WorldPreset>, PresetBuilder> BUILDERS = Maps.newHashMap();

    private static ResourceKey<WorldPreset> NORMAL = ResourceKey.create(
            WORLD_PRESET_REGISTRY,
            WorldsTogether.makeID("vanilla_normal")
    );
    private static ResourceKey<WorldPreset> LARGE_BIOMES = ResourceKey.create(
            WORLD_PRESET_REGISTRY,
            WorldsTogether.makeID("vanilla_large_biomes")
    );
    private static ResourceKey<WorldPreset> AMPLIFIED = ResourceKey.create(
            WORLD_PRESET_REGISTRY,
            WorldsTogether.makeID("vanilla_amplified")
    );
    private static ResourceKey<WorldPreset> FLAT = ResourceKey.create(
            WORLD_PRESET_REGISTRY,
            WorldsTogether.makeID("vanilla_flat")
    );
    private static ResourceKey<WorldPreset> SINGLE_BIOME_SURFACE = ResourceKey.create(
            WORLD_PRESET_REGISTRY,
            WorldsTogether.makeID("vanilla_single_biome_surface")
    );
    private static ResourceKey<WorldPreset> DEFAULT = NORMAL;

    public static Holder<WorldPreset> get(RegistryAccess access, ResourceKey<WorldPreset> key) {
        return ((access != null) ? access : BuiltinRegistries.ACCESS)
                .registryOrThrow(WORLD_PRESET_REGISTRY)
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
        ResourceKey<WorldPreset> key = ResourceKey.create(WORLD_PRESET_REGISTRY, loc);
        if (visibleInUI) {
            if (!didExplicitlySetDefault && DEFAULT == NORMAL) {
                DEFAULT = key;
            }
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
            BuiltinRegistries.register(presets, e.getKey(), preset);
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
                WorldGenUtil.Context endContext
        );
    }

    static {
        Registry.register(
                WORLD_PRESET,
                NORMAL,
                new WorldPreset(net.minecraft.client.gui.screens.worldselection.WorldPreset.NORMAL)
        );

        Registry.register(
                WORLD_PRESET,
                LARGE_BIOMES,
                new WorldPreset(net.minecraft.client.gui.screens.worldselection.WorldPreset.LARGE_BIOMES)
        );


        Registry.register(
                WORLD_PRESET,
                AMPLIFIED,
                new WorldPreset(net.minecraft.client.gui.screens.worldselection.WorldPreset.AMPLIFIED)
        );


        Registry.register(
                WORLD_PRESET,
                FLAT,
                new WorldPreset(net.minecraft.client.gui.screens.worldselection.WorldPreset.FLAT)
        );


        Registry.register(
                WORLD_PRESET,
                SINGLE_BIOME_SURFACE,
                new WorldPreset(net.minecraft.client.gui.screens.worldselection.WorldPreset.SINGLE_BIOME_SURFACE)
        );
    }
}
