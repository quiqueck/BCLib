package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Map;

public class PresetsRegistry {
    public static ResourceKey<WorldPreset> BCL_WORLD;
    public static ResourceKey<WorldPreset> BCL_WORLD_17;

    public static void onLoad() {
        BCL_WORLD =
                WorldPresets.register(
                        BCLib.makeID("normal"),
                        (overworldStem, netherContext, endContext) ->
                                buildPreset(
                                        overworldStem,
                                        netherContext,
                                        BCLNetherBiomeSourceConfig.DEFAULT, endContext,
                                        BCLEndBiomeSourceConfig.DEFAULT
                                ),
                        true
                );

        BCL_WORLD_17 = WorldPresets.register(
                BCLib.makeID("legacy_17"),
                (overworldStem, netherContext, endContext) ->
                        buildPreset(
                                overworldStem,
                                netherContext,
                                BCLNetherBiomeSourceConfig.MINECRAFT_17, endContext,
                                BCLEndBiomeSourceConfig.MINECRAFT_17
                        ),
                false
        );

        WorldPresets.setDEFAULT(BCL_WORLD);
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
