package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;
import org.betterx.bclib.mixin.common.ChunkGeneratorAccessor;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.biomesource.MergeableBiomeSource;
import org.betterx.worlds.together.chunkgenerator.EnforceableChunkGenerator;
import org.betterx.worlds.together.chunkgenerator.InjectableSurfaceRules;
import org.betterx.worlds.together.chunkgenerator.RestorableBiomeSource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.google.common.base.Suppliers;

import java.util.List;
import java.util.function.Function;

public class BCLChunkGenerator extends NoiseBasedChunkGenerator implements RestorableBiomeSource<BCLChunkGenerator>, InjectableSurfaceRules<BCLChunkGenerator>, EnforceableChunkGenerator<BCLChunkGenerator> {

    public static final Codec<BCLChunkGenerator> CODEC = RecordCodecBuilder
            .create((RecordCodecBuilder.Instance<BCLChunkGenerator> builderInstance) -> {
                final RecordCodecBuilder<BCLChunkGenerator, Registry<NormalNoise.NoiseParameters>> noiseGetter = RegistryOps
                        .retrieveRegistry(
                                Registry.NOISE_REGISTRY)
                        .forGetter(
                                BCLChunkGenerator::getNoises);

                RecordCodecBuilder<BCLChunkGenerator, BiomeSource> biomeSourceCodec = BiomeSource.CODEC
                        .fieldOf("biome_source")
                        .forGetter((BCLChunkGenerator generator) -> generator.biomeSource);

                RecordCodecBuilder<BCLChunkGenerator, Holder<NoiseGeneratorSettings>> settingsCodec = NoiseGeneratorSettings.CODEC
                        .fieldOf("settings")
                        .forGetter((BCLChunkGenerator generator) -> generator.settings);


                return NoiseBasedChunkGenerator
                        .commonCodec(builderInstance)
                        .and(builderInstance.group(noiseGetter, biomeSourceCodec, settingsCodec))
                        .apply(builderInstance, builderInstance.stable(BCLChunkGenerator::new));
            });
    public final BiomeSource initialBiomeSource;

    public BCLChunkGenerator(
            Registry<StructureSet> registry,
            Registry<NormalNoise.NoiseParameters> registry2,
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> holder
    ) {
        super(registry, registry2, biomeSource, holder);
        initialBiomeSource = biomeSource;
        if (biomeSource instanceof BCLBiomeSource bcl) {
            bcl.setMaxHeight(holder.value().noiseSettings().height());
        }

        if (WorldsTogether.RUNS_TERRABLENDER) {
            BCLib.LOGGER.info("Make sure features are loaded from terrablender for " + biomeSource);

            //terrablender is invalidating the feature initialization
            //we redo it at this point, otherwise we will get blank biomes
            rebuildFeaturesPerStep(biomeSource);
        }
        System.out.println("Chunk Generator: " + this + " (biomeSource: " + biomeSource + ")");
    }

    private void rebuildFeaturesPerStep(BiomeSource biomeSource) {
        if (this instanceof ChunkGeneratorAccessor acc) {
            Function<Holder<Biome>, BiomeGenerationSettings> function = (Holder<Biome> hh) -> hh.value()
                                                                                                .getGenerationSettings();

            acc.bcl_setFeaturesPerStep(Suppliers.memoize(() -> FeatureSorter.buildFeaturesPerStep(
                    List.copyOf(biomeSource.possibleBiomes()),
                    (hh) -> function.apply(hh).features(),
                    true
            )));
        }
    }

    /**
     * Other Mods like TerraBlender might inject new BiomeSources. We und that change after the world setup did run.
     *
     * @param dimensionKey The Dimension where this ChunkGenerator is used from
     */
    @Override
    public void restoreInitialBiomeSource(ResourceKey<LevelStem> dimensionKey) {
        if (initialBiomeSource != getBiomeSource()) {
            if (this instanceof ChunkGeneratorAccessor acc) {
                if (initialBiomeSource instanceof MergeableBiomeSource bs) {
                    acc.bcl_setBiomeSource(bs.mergeWithBiomeSource(getBiomeSource()));
                }

                rebuildFeaturesPerStep(getBiomeSource());
            }
        }
    }


    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }


    private Registry<NormalNoise.NoiseParameters> getNoises() {
        if (this instanceof NoiseGeneratorSettingsProvider p) {
            return p.bclib_getNoises();
        }
        return null;
    }

    @Override
    public String toString() {
        return "BCLib - Chunk Generator (" + Integer.toHexString(hashCode()) + ")";
    }

    // This method is injected by Terrablender.
    // We make sure terrablender does not rewrite the feature-set for our ChunkGenerator by overwriting the
    // Mixin-Method with an empty implementation
    public void appendFeaturesPerStep() {
    }

    public static RandomState createRandomState(ServerLevel level, ChunkGenerator generator) {
        if (generator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
            return RandomState.create(
                    noiseBasedChunkGenerator.generatorSettings().value(),
                    level.registryAccess().registryOrThrow(Registry.NOISE_REGISTRY),
                    level.getSeed()
            );
        } else {
            return RandomState.create(level.registryAccess(), NoiseGeneratorSettings.OVERWORLD, level.getSeed());
        }
    }

    @Override
    public WorldGenSettings enforceGeneratorInWorldGenSettings(
            RegistryAccess access,
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            ChunkGenerator loadedChunkGenerator,
            WorldGenSettings settings
    ) {
        BCLib.LOGGER.info("Enforcing Correct Generator for " + dimensionKey.location().toString() + ".");

        ChunkGenerator referenceGenerator = this;
        if (loadedChunkGenerator instanceof org.betterx.bclib.interfaces.ChunkGeneratorAccessor generator) {
            if (loadedChunkGenerator instanceof NoiseGeneratorSettingsProvider noiseProvider) {
                if (referenceGenerator instanceof NoiseGeneratorSettingsProvider referenceProvider) {
                    final BiomeSource bs;
                    if (referenceGenerator.getBiomeSource() instanceof MergeableBiomeSource mbs) {
                        bs = mbs.mergeWithBiomeSource(loadedChunkGenerator.getBiomeSource());
                    } else {
                        bs = referenceGenerator.getBiomeSource();
                    }

                    referenceGenerator = new BCLChunkGenerator(
                            generator.bclib_getStructureSetsRegistry(),
                            noiseProvider.bclib_getNoises(),
                            bs,
                            buildGeneratorSettings(
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

    }

    private static Holder<NoiseGeneratorSettings> buildGeneratorSettings(
            Holder<NoiseGeneratorSettings> reference,
            Holder<NoiseGeneratorSettings> settings,
            BiomeSource biomeSource
    ) {
        return settings;
//        NoiseGeneratorSettings old = settings.value();
//        NoiseGeneratorSettings noise = new NoiseGeneratorSettings(
//                old.noiseSettings(),
//                old.defaultBlock(),
//                old.defaultFluid(),
//                old.noiseRouter(),
//                SurfaceRuleRegistry.mergeSurfaceRulesFromBiomes(old.surfaceRule(), biomeSource),
//                //SurfaceRuleUtil.addRulesForBiomeSource(old.surfaceRule(), biomeSource),
//                old.spawnTarget(),
//                old.seaLevel(),
//                old.disableMobGeneration(),
//                old.aquifersEnabled(),
//                old.oreVeinsEnabled(),
//                old.useLegacyRandom()
//        );
//
//
//        return Holder.direct(noise);
    }
}
