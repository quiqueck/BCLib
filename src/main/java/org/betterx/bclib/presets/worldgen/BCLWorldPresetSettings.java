package org.betterx.bclib.presets.worldgen;

import org.betterx.bclib.api.v2.generator.BCLBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.Map;
import java.util.Set;

public class BCLWorldPresetSettings extends WorldPresetSettings {
    public final static BCLWorldPresetSettings DEFAULT = new BCLWorldPresetSettings(BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION);

    public static final Codec<BCLWorldPresetSettings> CODEC = RecordCodecBuilder
            .create((RecordCodecBuilder.Instance<BCLWorldPresetSettings> builderInstance) -> builderInstance.group(
                                                                                                                    Codec.INT
                                                                                                                            .fieldOf(LevelStem.NETHER.location().toString())
                                                                                                                            .forGetter(o -> o.netherVersion),
                                                                                                                    Codec.INT
                                                                                                                            .fieldOf(LevelStem.END.location().toString())
                                                                                                                            .forGetter(o -> o.endVersion),
                                                                                                                    Codec.BOOL
                                                                                                                            .fieldOf("custom_end_terrain")
                                                                                                                            .orElse(true)
                                                                                                                            .forGetter(o -> o.useEndTerrainGenerator),
                                                                                                                    Codec.BOOL
                                                                                                                            .fieldOf("generate_end_void")
                                                                                                                            .orElse(false)
                                                                                                                            .forGetter(o -> o.generateEndVoid)
                                                                                                            )
                                                                                                            .apply(
                                                                                                                    builderInstance,
                                                                                                                    builderInstance.stable(
                                                                                                                            BCLWorldPresetSettings::new)
                                                                                                            ));
    public final int netherVersion;
    public final int endVersion;
    public final boolean useEndTerrainGenerator;
    public final boolean generateEndVoid;

    public BCLWorldPresetSettings(int version) {
        this(version, version, true, false);
    }

    public BCLWorldPresetSettings(
            int netherVersion,
            int endVersion,
            boolean useEndTerrainGenerator,
            boolean generateEndVoid
    ) {
        this.netherVersion = netherVersion;
        this.endVersion = endVersion;
        this.useEndTerrainGenerator = endVersion != BCLibEndBiomeSource.BIOME_SOURCE_VERSION_VANILLA && useEndTerrainGenerator;
        this.generateEndVoid = generateEndVoid;
    }


    @Override
    public Codec<? extends WorldPresetSettings> codec() {
        return CODEC;
    }

    public TogetherWorldPreset buildPreset(
            LevelStem overworldStem,
            WorldGenUtil.Context netherContext,
            WorldGenUtil.Context endContext
    ) {
        return new TogetherWorldPreset(buildDimensionMap(overworldStem, netherContext, endContext), 1000, this);
    }

    private static BCLEndBiomeSourceConfig getEndConfigForVersion(int version) {
        if (version == BCLBiomeSource.BIOME_SOURCE_VERSION_HEX)
            return BCLEndBiomeSourceConfig.MINECRAFT_18;
        if (version == BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE)
            return BCLEndBiomeSourceConfig.MINECRAFT_17;

        return BCLEndBiomeSourceConfig.DEFAULT;
    }

    private static BCLNetherBiomeSourceConfig getNetherConfigForVersion(int version) {
        if (version == BCLBiomeSource.BIOME_SOURCE_VERSION_HEX)
            return BCLNetherBiomeSourceConfig.MINECRAFT_18;
        if (version == BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE)
            return BCLNetherBiomeSourceConfig.MINECRAFT_17;

        return BCLNetherBiomeSourceConfig.DEFAULT;
    }

    public Map<ResourceKey<LevelStem>, LevelStem> buildDimensionMap(
            LevelStem overworldStem,
            WorldGenUtil.Context netherContext,
            WorldGenUtil.Context endContext
    ) {
        return Map.of(
                LevelStem.OVERWORLD,
                overworldStem,
                LevelStem.NETHER,
                LevelGenUtil.getBCLNetherLevelStem(netherContext, getNetherConfigForVersion(netherVersion)),
                LevelStem.END,
                LevelGenUtil.getBCLEndLevelStem(endContext, getEndConfigForVersion(endVersion))
        );
    }

//    public int getVersion(ResourceKey<LevelStem> key) {
//        if (key == LevelStem.NETHER) return netherVersion;
//        if (key == LevelStem.END) return endVersion;
//
//        return BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA;
//    }


    public BiomeSource addDatapackBiomes(BiomeSource biomeSource, Set<Holder<Biome>> datapackBiomes) {
        if (biomeSource instanceof BCLBiomeSource bs) {
            return bs.createCopyForDatapack(datapackBiomes);
        }
        return biomeSource;
    }


//    private static Holder<NoiseGeneratorSettings> buildGeneratorSettings(
//            Holder<NoiseGeneratorSettings> reference,
//            Holder<NoiseGeneratorSettings> settings,
//            BiomeSource biomeSource
//    ) {
//        //SurfaceRuleUtil.injectSurfaceRules(settings.value(), biomeSource);
//        return settings;
////        NoiseGeneratorSettings old = settings.value();
////        NoiseGeneratorSettings noise = new NoiseGeneratorSettings(
////                old.noiseSettings(),
////                old.defaultBlock(),
////                old.defaultFluid(),
////                old.noiseRouter(),
////                SurfaceRuleRegistry.mergeSurfaceRulesFromBiomes(old.surfaceRule(), biomeSource),
////                //SurfaceRuleUtil.addRulesForBiomeSource(old.surfaceRule(), biomeSource),
////                old.spawnTarget(),
////                old.seaLevel(),
////                old.disableMobGeneration(),
////                old.aquifersEnabled(),
////                old.oreVeinsEnabled(),
////                old.useLegacyRandomSource()
////        );
////
////
////        return Holder.direct(noise);
//    }


//    /**
//     * Datapacks can change the world's generator. This Method will ensure, that the Generators contain
//     * the correct BiomeSources for this world
//     *
//     * @param dimensionKey
//     * @param dimensionTypeKey
//     * @param settings
//     * @return
//     */
//    private WorldGenSettings fixSettingsInCurrentWorld(
//            RegistryAccess access, ResourceKey<LevelStem> dimensionKey,
//            ResourceKey<DimensionType> dimensionTypeKey,
//            WorldGenSettings settings
//    ) {
//        Optional<Holder<LevelStem>> loadedStem = settings.dimensions().getHolder(dimensionKey);
//        final ChunkGenerator loadedChunkGenerator = loadedStem.map(h -> h.value().generator()).orElse(null);
//        final int loaderVersion = LevelGenUtil.getBiomeVersionForGenerator(loadedStem
//                .map(h -> h.value().generator())
//                .orElse(null));
//
//        final int targetVersion = getVersion(dimensionKey);
//        if (loaderVersion != targetVersion) {
//            BCLib.LOGGER.info("Enforcing Correct Generator for " + dimensionKey.location().toString() + ".");
//
//            Optional<Holder<LevelStem>> refLevelStem = LevelGenUtil.referenceStemForVersion(
//                    dimensionKey,
//                    targetVersion,
//                    access,
//                    settings.seed(),
//                    settings.generateStructures(),
//                    settings.generateBonusChest()
//            );
//
//            ChunkGenerator referenceGenerator = refLevelStem.map(h -> h.value().generator()).orElse(null);
//            if (referenceGenerator == null) {
//                BCLib.LOGGER.error("Failed to create Generator for " + dimensionKey.location().toString());
//                return settings;
//            }
//
//            if (loadedChunkGenerator instanceof ChunkGeneratorAccessor generator) {
//                if (loadedChunkGenerator instanceof NoiseGeneratorSettingsProvider noiseProvider) {
//                    if (referenceGenerator instanceof NoiseGeneratorSettingsProvider referenceProvider) {
//                        final Set<Holder<Biome>> biomes = loadedChunkGenerator.getBiomeSource().possibleBiomes();
//                        final BiomeSource bs = addDatapackBiomes(referenceGenerator.getBiomeSource(), biomes);
//                        InternalBiomeAPI.applyModifications(bs, dimensionKey);
//                        referenceGenerator = new BCLChunkGenerator(
//                                generator.bclib_getStructureSetsRegistry(),
//                                noiseProvider.bclib_getNoises(),
//                                bs,
//                                buildGeneratorSettings(
//                                        referenceProvider.bclib_getNoiseGeneratorSettingHolders(),
//                                        noiseProvider.bclib_getNoiseGeneratorSettingHolders(),
//                                        bs
//                                )
//                        );
//                    }
//                }
//            }
//
//            return LevelGenUtil.replaceGenerator(
//                    dimensionKey,
//                    dimensionTypeKey,
//                    access,
//                    settings,
//                    referenceGenerator
//            );
//        }
//        return settings;
//    }

    public WorldGenSettings repairSettingsOnLoad(RegistryAccess registryAccess, WorldGenSettings settings) {
//        settings = fixSettingsInCurrentWorld(registryAccess, LevelStem.NETHER, BuiltinDimensionTypes.NETHER, settings);
//        settings = fixSettingsInCurrentWorld(registryAccess, LevelStem.END, BuiltinDimensionTypes.END, settings);
        return settings;
    }

}
