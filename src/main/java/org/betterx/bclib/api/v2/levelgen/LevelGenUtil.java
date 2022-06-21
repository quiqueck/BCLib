package org.betterx.bclib.api.v2.levelgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.BCLBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLChunkGenerator;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.presets.worldgen.BCLWorldPresetSettings;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.worlds.together.util.ModUtil;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class LevelGenUtil {
    private static final String TAG_VERSION = "version";
    private static final String TAG_BN_GEN_VERSION = "generator_version";

    @NotNull
    public static LevelStem getBCLNetherLevelStem(WorldGenUtil.Context context, Optional<Integer> version) {
        BCLibNetherBiomeSource netherSource = new BCLibNetherBiomeSource(context.biomes, version);
        return getBCLNetherLevelStem(context, netherSource);
    }

    public static LevelStem getBCLNetherLevelStem(WorldGenUtil.StemContext context, BiomeSource biomeSource) {
        return new LevelStem(
                context.dimension,
                new BCLChunkGenerator(
                        context.structureSets,
                        context.noiseParameters,
                        biomeSource,
                        context.generatorSettings
                )
        );
    }

    @NotNull
    public static LevelStem getBCLEndLevelStem(WorldGenUtil.StemContext context, BiomeSource biomeSource) {
        return new LevelStem(
                context.dimension,
                new BCLChunkGenerator(
                        context.structureSets,
                        context.noiseParameters,
                        biomeSource,
                        context.generatorSettings
                )
        );
    }

    public static LevelStem getBCLEndLevelStem(WorldGenUtil.Context context, Optional<Integer> version) {
        BCLibEndBiomeSource endSource = new BCLibEndBiomeSource(context.biomes, version);
        return getBCLEndLevelStem(context, endSource);
    }


    public static WorldGenSettings replaceGenerator(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            int biomeSourceVersion,
            RegistryAccess registryAccess,
            WorldGenSettings worldGenSettings
    ) {
        Optional<Holder<LevelStem>> oLevelStem = referenceStemForVersion(
                dimensionKey,
                biomeSourceVersion,
                registryAccess,
                worldGenSettings.seed(),
                worldGenSettings.generateStructures(),
                worldGenSettings.generateStructures()
        );
        return replaceGenerator(
                dimensionKey,
                dimensionTypeKey,
                registryAccess,
                worldGenSettings,
                oLevelStem.map(l -> l.value().generator()).orElseThrow()
        );
    }

    public static WorldGenSettings replaceGenerator(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            RegistryAccess registryAccess,
            WorldGenSettings worldGenSettings,
            ChunkGenerator generator
    ) {
        Registry<DimensionType> dimensionTypeRegistry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<LevelStem> newDimensions = withDimension(
                dimensionKey,
                dimensionTypeKey,
                dimensionTypeRegistry,
                worldGenSettings.dimensions(),
                generator
        );
        return new WorldGenSettings(
                worldGenSettings.seed(),
                worldGenSettings.generateStructures(),
                worldGenSettings.generateBonusChest(),
                newDimensions
        );
    }

    public static WorldGenSettings replaceStem(
            ResourceKey<LevelStem> dimensionKey,
            WorldGenSettings worldGenSettings,
            LevelStem levelStem
    ) {
        Registry<LevelStem> newDimensions = withDimension(
                dimensionKey,
                worldGenSettings.dimensions(),
                levelStem
        );
        return new WorldGenSettings(
                worldGenSettings.seed(),
                worldGenSettings.generateStructures(),
                worldGenSettings.generateBonusChest(),
                newDimensions
        );
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
                ? dimensionTypeRegistry.getOrCreateHolderOrThrow(dimensionTypeKey)
                : levelStem.typeHolder();
        return withDimension(dimensionKey, inputDimensions, new LevelStem(dimensionType, generator));
    }

    public static Registry<LevelStem> withDimension(
            ResourceKey<LevelStem> dimensionKey,
            Registry<LevelStem> inputDimensions,
            LevelStem levelStem
    ) {
        MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<>(
                Registry.LEVEL_STEM_REGISTRY,
                Lifecycle.experimental(),
                null
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

    public static int getBiomeVersionForGenerator(ChunkGenerator generator) {
        if (generator == null) return BCLBiomeSource.getVersionBiomeSource(null);
        return BCLBiomeSource.getVersionBiomeSource(generator.getBiomeSource());
    }

    public static Optional<Holder<LevelStem>> referenceStemForVersion(
            ResourceKey<LevelStem> dimensionKey,
            int biomeSourceVersion,
            RegistryAccess registryAccess,
            long seed,
            boolean generateStructures,
            boolean generateBonusChest
    ) {
        final WorldGenSettings referenceSettings;
        if (biomeSourceVersion == BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA) {
            referenceSettings = net.minecraft.world.level.levelgen.presets.WorldPresets.createNormalWorldFromPreset(
                    registryAccess,
                    seed,
                    generateStructures,
                    generateBonusChest
            );
        } else if (biomeSourceVersion == BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE) {
            referenceSettings = WorldGenUtil.createWorldFromPreset(
                    PresetsRegistry.BCL_WORLD_17,
                    registryAccess,
                    seed,
                    generateStructures,
                    generateBonusChest
            );
        } else {
            referenceSettings = WorldGenUtil.createDefaultWorldFromPreset(
                    registryAccess,
                    seed,
                    generateStructures,
                    generateBonusChest
            );
        }
        return referenceSettings.dimensions().getHolder(dimensionKey);
    }

    public static int getBiomeVersionForCurrentWorld(ResourceKey<LevelStem> key) {
        final CompoundTag settingsNbt = WorldGenUtil.getSettingsNbt();
        if (!settingsNbt.contains(key.location().toString())) return BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION;
        return settingsNbt.getInt(key.location().toString());
    }

    private static int getDimensionVersion(
            WorldGenSettings settings,
            ResourceKey<LevelStem> key
    ) {
        var dimension = settings.dimensions().getHolder(key);
        if (dimension.isPresent()) {
            return getBiomeVersionForGenerator(dimension.get().value().generator());
        } else {
            return getBiomeVersionForGenerator(null);
        }
    }

    private static void writeDimensionVersion(
            WorldGenSettings settings,
            CompoundTag generatorSettings,
            ResourceKey<LevelStem> key
    ) {
        generatorSettings.putInt(key.location().toString(), getDimensionVersion(settings, key));
    }

    public static void migrateGeneratorSettings() {
        final CompoundTag settingsNbt = WorldGenUtil.getSettingsNbt();

        if (settingsNbt.size() == 0) {
            BCLib.LOGGER.info("Found World without generator Settings. Setting up data...");
            int biomeSourceVersion = BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION;

            final CompoundTag bclRoot = WorldConfig.getRootTag(BCLib.MOD_ID);

            String bclVersion = "0.0.0";
            if (bclRoot.contains(TAG_VERSION)) {
                bclVersion = bclRoot.getString(TAG_VERSION);
            }
            boolean isPre18 = !ModUtil.isLargerOrEqualVersion(bclVersion, "1.0.0");

            if (isPre18) {
                BCLib.LOGGER.info("World was create pre 1.18!");
                biomeSourceVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE;
            }

            if (WorldConfig.hasMod("betternether")) {
                BCLib.LOGGER.info("Found Data from BetterNether, using for migration.");
                final CompoundTag bnRoot = WorldConfig.getRootTag("betternether");
                biomeSourceVersion = "1.17".equals(bnRoot.getString(TAG_BN_GEN_VERSION))
                        ? BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE
                        : BCLBiomeSource.BIOME_SOURCE_VERSION_HEX;
            }

            BCLib.LOGGER.info("Set world to BiomeSource Version " + biomeSourceVersion);
            TogetherWorldPreset.writeWorldPresetSettings(new BCLWorldPresetSettings(
                    biomeSourceVersion,
                    biomeSourceVersion,
                    true,
                    true
            ));
        }
    }
}
