package org.betterx.bclib.api.v2.levelgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.BCLChunkGenerator;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.util.ModUtil;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class LevelGenUtil {
    private static final String TAG_VERSION = "version";
    private static final String TAG_BN_GEN_VERSION = "generator_version";

    @NotNull
    public static LevelStem getBCLNetherLevelStem(WorldGenUtil.Context context, BCLNetherBiomeSourceConfig config) {
        BCLibNetherBiomeSource netherSource = new BCLibNetherBiomeSource(config);

        return new LevelStem(
                context.dimension,
                new BCLChunkGenerator(
                        netherSource,
                        context.generatorSettings
                )
        );
    }

    public static LevelStem getBCLEndLevelStem(WorldGenUtil.Context context, BCLEndBiomeSourceConfig config) {
        BCLibEndBiomeSource endSource = new BCLibEndBiomeSource(config);
        return new LevelStem(
                context.dimension,
                new BCLChunkGenerator(
                        endSource,
                        context.generatorSettings
                )
        );
    }


    public static Registry<LevelStem> replaceGenerator(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            RegistryAccess registryAccess,
            Registry<LevelStem> dimensionRegistry,
            ChunkGenerator generator
    ) {
        Registry<DimensionType> dimensionTypeRegistry = registryAccess.registryOrThrow(Registries.DIMENSION_TYPE);
        Registry<LevelStem> newDimensions = withDimension(
                dimensionKey,
                dimensionTypeKey,
                dimensionTypeRegistry,
                dimensionRegistry,
                generator
        );
        return newDimensions;
    }

    public static Registry<LevelStem> withDimension(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            Registry<DimensionType> dimensionTypeRegistry,
            Registry<LevelStem> inputDimensions,
            ChunkGenerator generator
    ) {

        LevelStem levelStem = inputDimensions.get(dimensionKey);
        Holder<DimensionType> dimensionType = levelStem == null
                ? dimensionTypeRegistry.getHolderOrThrow(dimensionTypeKey)
                : levelStem.type();
        return withDimension(dimensionKey, inputDimensions, new LevelStem(dimensionType, generator));
    }

    public static Registry<LevelStem> withDimension(
            ResourceKey<LevelStem> dimensionKey,
            Registry<LevelStem> inputDimensions,
            LevelStem levelStem
    ) {
        MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<>(
                Registries.LEVEL_STEM,
                Lifecycle.experimental()
        );
        writableRegistry.register(
                dimensionKey,
                levelStem,
                Lifecycle.stable()
        );
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : inputDimensions.entrySet()) {
            ResourceKey<LevelStem> resourceKey = entry.getKey();
            if (resourceKey == dimensionKey) continue;
            writableRegistry.register(
                    resourceKey,
                    entry.getValue(),
                    inputDimensions.lifecycle(entry.getValue())
            );
        }
        return writableRegistry;
    }


    public static void migrateGeneratorSettings() {
        final CompoundTag settingsNbt = WorldGenUtil.getPresetsNbt();

        if (settingsNbt.size() == 0) {
            CompoundTag oldGen = WorldGenUtil.getGeneratorNbt();
            if (oldGen != null) {
                if (oldGen.contains("type")) {
                    BCLib.LOGGER.info("Found World with beta generator Settings.");
                    if ("bclib:bcl_world_preset_settings".equals(oldGen.getString("type"))) {
                        int netherVersion = 18;
                        int endVersion = 18;
                        if (oldGen.contains("minecraft:the_nether"))
                            netherVersion = oldGen.getInt("minecraft:the_nether");
                        if (oldGen.contains("minecraft:the_end"))
                            endVersion = oldGen.getInt("minecraft:the_end");

                        if (netherVersion == 18) netherVersion = 0;
                        else if (netherVersion == 17) netherVersion = 1;
                        else netherVersion = 2;

                        if (endVersion == 18) endVersion = 0;
                        else if (endVersion == 17) endVersion = 1;
                        else endVersion = 2;

                        var presets = List.of(
                                TogetherWorldPreset.getDimensionsMap(PresetsRegistry.BCL_WORLD),
                                TogetherWorldPreset.getDimensionsMap(PresetsRegistry.BCL_WORLD_17),
                                TogetherWorldPreset.getDimensionsMap(WorldPresets.NORMAL)
                        );
                        Map<ResourceKey<LevelStem>, ChunkGenerator> dimensions = new HashMap<>();
                        dimensions.put(LevelStem.OVERWORLD, presets.get(0).get(LevelStem.OVERWORLD));
                        dimensions.put(LevelStem.NETHER, presets.get(netherVersion).get(LevelStem.NETHER));
                        dimensions.put(LevelStem.END, presets.get(endVersion).get(LevelStem.END));

                        TogetherWorldPreset.writeWorldPresetSettingsDirect(dimensions);
                    }
                    return;
                }
            }

            BCLib.LOGGER.info("Found World without generator Settings. Setting up data...");
            ResourceKey<WorldPreset> biomeSourceVersion = PresetsRegistry.BCL_WORLD;

            final CompoundTag bclRoot = WorldConfig.getRootTag(BCLib.MOD_ID);

            String bclVersion = "0.0.0";
            if (bclRoot.contains(TAG_VERSION)) {
                bclVersion = bclRoot.getString(TAG_VERSION);
            }
            boolean isPre18 = !ModUtil.isLargerOrEqualVersion(bclVersion, "1.0.0");

            if (isPre18) {
                BCLib.LOGGER.info("World was create pre 1.18!");
                biomeSourceVersion = PresetsRegistry.BCL_WORLD_17;
            }

            if (WorldConfig.hasMod("betternether")) {
                BCLib.LOGGER.info("Found Data from BetterNether, using for migration.");
                final CompoundTag bnRoot = WorldConfig.getRootTag("betternether");
                biomeSourceVersion = "1.17".equals(bnRoot.getString(TAG_BN_GEN_VERSION))
                        ? PresetsRegistry.BCL_WORLD_17
                        : PresetsRegistry.BCL_WORLD;
            }

            Registry<LevelStem> dimensions = TogetherWorldPreset.getDimensions(biomeSourceVersion);
            if (dimensions != null) {
                BCLib.LOGGER.info("Set world to BiomeSource Version " + biomeSourceVersion);
                TogetherWorldPreset.writeWorldPresetSettings(dimensions);
            } else {
                BCLib.LOGGER.error("Failed to set world to BiomeSource Version " + biomeSourceVersion);
            }
        }
    }
}
