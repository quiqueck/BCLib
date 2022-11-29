package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.BCLChunkGenerator;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.entrypoints.WorldPresetBootstrap;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Map;

public class PresetsRegistry implements WorldPresetBootstrap {

    public static ResourceKey<WorldPreset> BCL_WORLD;
    public static ResourceKey<WorldPreset> BCL_WORLD_LARGE;
    public static ResourceKey<WorldPreset> BCL_WORLD_AMPLIFIED;
    public static ResourceKey<WorldPreset> BCL_WORLD_17;

    public void bootstrapWorldPresets() {
        BCL_WORLD =
                WorldPresets.register(
                        BCLib.makeID("normal"),
                        (overworldStem, netherContext, endContext, noiseSettings, noiseBasedOverworld) ->
                                buildPreset(
                                        overworldStem,
                                        netherContext, BCLNetherBiomeSourceConfig.DEFAULT,
                                        endContext, BCLEndBiomeSourceConfig.DEFAULT
                                ),
                        true
                );

        BCL_WORLD_LARGE =
                WorldPresets.register(
                        BCLib.makeID("large"),
                        (overworldStem, netherContext, endContext, noiseSettings, noiseBasedOverworld) -> {
                            Holder<NoiseGeneratorSettings> largeBiomeGenerator = noiseSettings
                                    .getOrCreateHolderOrThrow(NoiseGeneratorSettings.LARGE_BIOMES);
                            return buildPreset(
                                    noiseBasedOverworld.make(
                                            overworldStem.generator().getBiomeSource(),
                                            largeBiomeGenerator
                                    ),
                                    netherContext, BCLNetherBiomeSourceConfig.MINECRAFT_18_LARGE,
                                    endContext, BCLEndBiomeSourceConfig.MINECRAFT_18_LARGE
                            );
                        },
                        true
                );

        BCL_WORLD_AMPLIFIED = WorldPresets.register(
                BCLib.makeID("amplified"),
                (overworldStem, netherContext, endContext, noiseSettings, noiseBasedOverworld) -> {
                    Holder<NoiseGeneratorSettings> amplifiedBiomeGenerator = noiseSettings
                            .getOrCreateHolderOrThrow(NoiseGeneratorSettings.AMPLIFIED);

                    WorldGenUtil.Context amplifiedNetherContext = new WorldGenUtil.Context(
                            netherContext.biomes,
                            netherContext.dimension,
                            netherContext.structureSets,
                            netherContext.noiseParameters,
                            Holder.direct(BCLChunkGenerator.amplifiedNether())
                    );

                    return buildPreset(
                            noiseBasedOverworld.make(
                                    overworldStem.generator().getBiomeSource(),
                                    amplifiedBiomeGenerator
                            ),
                            amplifiedNetherContext, BCLNetherBiomeSourceConfig.MINECRAFT_18_AMPLIFIED,
                            endContext, BCLEndBiomeSourceConfig.MINECRAFT_18_AMPLIFIED
                    );
                },
                true
        );

        BCL_WORLD_17 = WorldPresets.register(
                BCLib.makeID("legacy_17"),
                (overworldStem, netherContext, endContext, noiseSettings, noiseBasedOverworld) ->
                        buildPreset(
                                overworldStem,
                                netherContext,
                                BCLNetherBiomeSourceConfig.MINECRAFT_17, endContext,
                                BCLEndBiomeSourceConfig.MINECRAFT_17
                        ),
                false
        );

        if (Configs.CLIENT_CONFIG.forceBetterXPreset())
            WorldPresets.setDEFAULT(BCL_WORLD);
        else
            WorldPresets.setDEFAULT(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);
    }

    public static TogetherWorldPreset buildPreset(
            LevelStem overworldStem,
            WorldGenUtil.Context netherContext,
            BCLNetherBiomeSourceConfig netherConfig,
            WorldGenUtil.Context endContext,
            BCLEndBiomeSourceConfig endConfig
    ) {
        return new TogetherWorldPreset(buildDimensionMap(
                overworldStem,
                netherContext,
                netherConfig, endContext,
                endConfig
        ), 1000);
    }

    public static Map<ResourceKey<LevelStem>, LevelStem> buildDimensionMap(
            LevelStem overworldStem,
            WorldGenUtil.Context netherContext,
            BCLNetherBiomeSourceConfig netherConfig,
            WorldGenUtil.Context endContext,
            BCLEndBiomeSourceConfig endConfig
    ) {
        return Map.of(
                LevelStem.OVERWORLD,
                overworldStem,
                LevelStem.NETHER,
                LevelGenUtil.getBCLNetherLevelStem(netherContext, netherConfig),
                LevelStem.END,
                LevelGenUtil.getBCLEndLevelStem(endContext, endConfig)
        );
    }
}
