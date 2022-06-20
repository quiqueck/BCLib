package org.betterx.bclib.presets.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.BCLBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLChunkGenerator;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.api.v2.levelgen.surface.SurfaceRuleUtil;
import org.betterx.bclib.interfaces.ChunkGeneratorAccessor;
import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BCLWorldPresetSettings extends WorldPresetSettings {
    public final static BCLWorldPresetSettings DEFAULT = new BCLWorldPresetSettings(BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION);

    public static final Codec<BCLWorldPresetSettings> CODEC = RecordCodecBuilder
            .create((RecordCodecBuilder.Instance<BCLWorldPresetSettings> builderInstance) -> {
                return builderInstance.group(
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
                                      .apply(builderInstance, builderInstance.stable(BCLWorldPresetSettings::new));
            });
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

    public BCLWorldPreset buildPreset(
            LevelStem overworldStem,
            LevelGenUtil.Context netherContext,
            LevelGenUtil.Context endContext
    ) {
        return new BCLWorldPreset(buildDimensionMap(overworldStem, netherContext, endContext), 1000, this);
    }

    public Map<ResourceKey<LevelStem>, LevelStem> buildDimensionMap(
            LevelStem overworldStem,
            LevelGenUtil.Context netherContext,
            LevelGenUtil.Context endContext
    ) {
        return Map.of(
                LevelStem.OVERWORLD,
                overworldStem,
                LevelStem.NETHER,
                createNetherStem(netherContext),
                LevelStem.END,
                createEndStem(endContext)
        );
    }

    public int getVersion(ResourceKey<LevelStem> key) {
        if (key == LevelStem.NETHER) return netherVersion;
        if (key == LevelStem.END) return endVersion;

        return BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA;
    }

    public LevelStem createStem(LevelGenUtil.Context ctx, ResourceKey<LevelStem> key) {
        if (key == LevelStem.NETHER) return createNetherStem(ctx);
        if (key == LevelStem.END) return createEndStem(ctx);
        return null;
    }

    public LevelStem createNetherStem(LevelGenUtil.Context ctx) {
        return LevelGenUtil.getBCLNetherLevelStem(ctx, Optional.of(netherVersion));
    }

    public LevelStem createEndStem(LevelGenUtil.Context ctx) {
        return LevelGenUtil.getBCLEndLevelStem(ctx, Optional.of(endVersion));
    }

    public BiomeSource fixBiomeSource(BiomeSource biomeSource, Set<Holder<Biome>> datapackBiomes) {
        if (biomeSource instanceof BCLBiomeSource bs) {
            return bs.createCopyForDatapack(datapackBiomes);
        }
        return biomeSource;
    }

    private Holder<NoiseGeneratorSettings> fixNoiseSettings(
            Holder<NoiseGeneratorSettings> reference,
            Holder<NoiseGeneratorSettings> settings,
            BiomeSource biomeSource
    ) {
        NoiseGeneratorSettings old = settings.value();
        NoiseGeneratorSettings noise = new NoiseGeneratorSettings(
                old.noiseSettings(),
                old.defaultBlock(),
                old.defaultFluid(),
                old.noiseRouter(),
                SurfaceRuleUtil.addRulesForBiomeSource(old.surfaceRule(), biomeSource),
                old.spawnTarget(),
                old.seaLevel(),
                old.disableMobGeneration(),
                old.aquifersEnabled(),
                old.oreVeinsEnabled(),
                old.useLegacyRandomSource()
        );


        return Holder.direct(noise);
    }


    /**
     * Datapacks can change the world's generator. This Method will ensure, that the Generators contain
     * the correct BiomeSources for this world
     *
     * @param dimensionKey
     * @param dimensionTypeKey
     * @param settings
     * @return
     */
    private WorldGenSettings fixSettingsInCurrentWorld(
            RegistryAccess access, ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            WorldGenSettings settings
    ) {
        Optional<Holder<LevelStem>> loadedStem = settings.dimensions().getHolder(dimensionKey);
        final ChunkGenerator loadedChunkGenerator = loadedStem.map(h -> h.value().generator()).orElse(null);
        final int loaderVersion = LevelGenUtil.getBiomeVersionForGenerator(loadedStem
                .map(h -> h.value().generator())
                .orElse(null));

        final int targetVersion = getVersion(dimensionKey);
        if (loaderVersion != targetVersion) {
            BCLib.LOGGER.info("Enforcing Correct Generator for " + dimensionKey.location().toString() + ".");

            Optional<Holder<LevelStem>> refLevelStem = LevelGenUtil.referenceStemForVersion(
                    dimensionKey,
                    targetVersion,
                    access,
                    settings.seed(),
                    settings.generateStructures(),
                    settings.generateBonusChest()
            );

            ChunkGenerator referenceGenerator = refLevelStem.map(h -> h.value().generator()).orElse(null);
            if (referenceGenerator == null) {
                BCLib.LOGGER.error("Failed to create Generator for " + dimensionKey.location().toString());
                return settings;
            }

            if (loadedChunkGenerator instanceof ChunkGeneratorAccessor generator) {
                if (loadedChunkGenerator instanceof NoiseGeneratorSettingsProvider noiseProvider) {
                    if (referenceGenerator instanceof NoiseGeneratorSettingsProvider referenceProvider) {
                        final Set<Holder<Biome>> biomes = loadedChunkGenerator.getBiomeSource().possibleBiomes();
                        final BiomeSource bs = fixBiomeSource(referenceGenerator.getBiomeSource(), biomes);
                        InternalBiomeAPI.applyModifications(bs, dimensionKey);
                        referenceGenerator = new BCLChunkGenerator(
                                generator.bclib_getStructureSetsRegistry(),
                                noiseProvider.bclib_getNoises(),
                                bs,
                                fixNoiseSettings(
                                        referenceProvider.bclib_getNoiseGeneratorSettingHolders(),
                                        noiseProvider.bclib_getNoiseGeneratorSettingHolders(),
                                        bs
                                )
                        );
                    }
                }
            }

            return LevelGenUtil.replaceGenerator(
                    dimensionKey,
                    dimensionTypeKey,
                    access,
                    settings,
                    referenceGenerator
            );
        } else {
            BCLChunkGenerator.injectNoiseSettings(dimensionKey, loadedChunkGenerator);
        }
        return settings;
    }

    public WorldGenSettings repairSettingsOnLoad(RegistryAccess registryAccess, WorldGenSettings settings) {
        settings = fixSettingsInCurrentWorld(registryAccess, LevelStem.NETHER, BuiltinDimensionTypes.NETHER, settings);
        settings = fixSettingsInCurrentWorld(registryAccess, LevelStem.END, BuiltinDimensionTypes.END, settings);
        BCLChunkGenerator.injectNoiseSettings(settings, BCLChunkGenerator.NON_MANAGED_DIMENSIONS);
        return settings;
    }

}
