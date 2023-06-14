package org.betterx.datagen.bclib.preset;

import org.betterx.bclib.api.v2.generator.BCLChunkGenerator;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WorldPresetDataProvider extends FabricTagProvider<WorldPreset> {


    /**
     * Constructs a new {@link FabricTagProvider} with the default computed path.
     *
     * <p>Common implementations of this class are provided.
     *
     * @param output           the {@link FabricDataOutput} instance
     * @param registriesFuture the backing registry for the tag type
     */
    public WorldPresetDataProvider(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, Registries.WORLD_PRESET, registriesFuture);
    }

    public static void bootstrap(BootstapContext<WorldPreset> bootstrapContext) {
        final WorldPresets.BootstrapData ctx = new WorldPresets.BootstrapData(bootstrapContext);

        bootstrapContext.register(PresetsRegistry.BCL_WORLD, createNormal(ctx));
        bootstrapContext.register(PresetsRegistry.BCL_WORLD_LARGE, createLarge(ctx));
        bootstrapContext.register(PresetsRegistry.BCL_WORLD_AMPLIFIED, createAmplified(ctx));
        bootstrapContext.register(PresetsRegistry.BCL_WORLD_17, createLegacy(ctx));
    }

    private static WorldPreset createLegacy(WorldPresets.BootstrapData ctx) {
        return buildPreset(
                ctx.overworldStem,
                ctx.netherContext,
                BCLNetherBiomeSourceConfig.MINECRAFT_17, ctx.endContext,
                BCLEndBiomeSourceConfig.MINECRAFT_17
        );
    }

    private static WorldPreset createAmplified(WorldPresets.BootstrapData ctx) {
        Holder<NoiseGeneratorSettings> amplifiedBiomeGenerator = ctx.noiseSettings
                .getOrThrow(NoiseGeneratorSettings.AMPLIFIED);

        WorldGenUtil.Context amplifiedNetherContext = new WorldGenUtil.Context(
                ctx.netherContext.dimension,
                ctx.netherContext.structureSets,
                ctx.noiseSettings.getOrThrow(BCLChunkGenerator.AMPLIFIED_NETHER)
        );

        return buildPreset(
                ctx.makeNoiseBasedOverworld(
                        ctx.overworldStem.generator().getBiomeSource(),
                        amplifiedBiomeGenerator
                ),
                amplifiedNetherContext, BCLNetherBiomeSourceConfig.MINECRAFT_18_AMPLIFIED,
                ctx.endContext, BCLEndBiomeSourceConfig.MINECRAFT_20_AMPLIFIED
        );
    }

    private static WorldPreset createLarge(WorldPresets.BootstrapData ctx) {
        Holder<NoiseGeneratorSettings> largeBiomeGenerator = ctx.noiseSettings
                .getOrThrow(NoiseGeneratorSettings.LARGE_BIOMES);
        return buildPreset(
                ctx.makeNoiseBasedOverworld(
                        ctx.overworldStem.generator().getBiomeSource(),
                        largeBiomeGenerator
                ),
                ctx.netherContext, BCLNetherBiomeSourceConfig.MINECRAFT_18_LARGE,
                ctx.endContext, BCLEndBiomeSourceConfig.MINECRAFT_20_LARGE
        );
    }

    private static WorldPreset createNormal(WorldPresets.BootstrapData ctx) {
        return buildPreset(
                ctx.overworldStem,
                ctx.netherContext, BCLNetherBiomeSourceConfig.DEFAULT,
                ctx.endContext, BCLEndBiomeSourceConfig.DEFAULT
        );
    }

    private static TogetherWorldPreset buildPreset(
            LevelStem overworldStem,
            WorldGenUtil.Context netherContext,
            BCLNetherBiomeSourceConfig netherConfig,
            WorldGenUtil.Context endContext,
            BCLEndBiomeSourceConfig endConfig
    ) {
        return new TogetherWorldPreset(buildDimensionMap(
                overworldStem, netherContext, netherConfig, endContext, endConfig
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

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        final FabricTagProvider<WorldPreset>.FabricTagBuilder builder = getOrCreateTagBuilder(WorldPresetTags.NORMAL);
        builder.add(PresetsRegistry.BCL_WORLD);
        builder.add(PresetsRegistry.BCL_WORLD_AMPLIFIED);
        builder.add(PresetsRegistry.BCL_WORLD_LARGE);
    }
}
