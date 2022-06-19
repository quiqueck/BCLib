package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.api.v2.levelgen.surface.SurfaceRuleUtil;
import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;
import org.betterx.bclib.interfaces.SurfaceRuleProvider;
import org.betterx.bclib.mixin.common.ChunkGeneratorAccessor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.google.common.base.Suppliers;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class BCLChunkGenerator extends NoiseBasedChunkGenerator {

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

        if (BCLib.RUNS_TERRABLENDER) {
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

    public void restoreInitialBiomeSource() {
        if (initialBiomeSource != getBiomeSource()) {
            if (this instanceof ChunkGeneratorAccessor acc) {
                BiomeSource bs = LevelGenUtil.getWorldSettings()
                                             .fixBiomeSource(initialBiomeSource, getBiomeSource().possibleBiomes());
                acc.bcl_setBiomeSource(bs);
                rebuildFeaturesPerStep(getBiomeSource());
            }
        }
    }


    public static void injectNoiseSettings(
            ResourceKey<LevelStem> dimensionKey,
            ChunkGenerator loadedChunkGenerator
    ) {
        BCLib.LOGGER.debug("Checking Noise Settings for " + dimensionKey.location().toString());
        final BiomeSource loadedBiomeSource = loadedChunkGenerator.getBiomeSource();
        InternalBiomeAPI.applyModifications(loadedBiomeSource, dimensionKey);

        if (loadedChunkGenerator instanceof NoiseBasedChunkGenerator nbc) {
            if (((Object) nbc.generatorSettings().value()) instanceof SurfaceRuleProvider srp) {
                srp.bclib_overwrite(SurfaceRuleUtil.addRulesForBiomeSource(nbc
                        .generatorSettings()
                        .value()
                        .surfaceRule(), loadedBiomeSource));
            }
        }
    }

    public static final Predicate<ResourceKey<LevelStem>> NON_MANAGED_DIMENSIONS = dim -> dim != LevelStem.NETHER && dim != LevelStem.END;

    public static void injectNoiseSettings(WorldGenSettings settings, Predicate<ResourceKey<LevelStem>> filter) {
        List<ResourceKey<LevelStem>> otherDimensions = settings
                .dimensions()
                .entrySet()
                .stream()
                .map(e -> e.getKey())
                .filter(filter)
                .toList();

        for (ResourceKey<LevelStem> key : otherDimensions) {
            Optional<Holder<LevelStem>> stem = settings.dimensions().getHolder(key);
            if (stem.isPresent()) {
                injectNoiseSettings(key, stem.get().value().generator());
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

    //Make sure terrablender does not rewrite the feature-set
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

    public static void restoreInitialBiomeSources(WorldGenSettings settings) {
        restoreInitialBiomeSource(settings, LevelStem.NETHER);
        restoreInitialBiomeSource(settings, LevelStem.END);
    }

    public static void restoreInitialBiomeSource(WorldGenSettings settings, ResourceKey<LevelStem> dimension) {
        LevelStem loadedStem = settings.dimensions().getOrThrow(dimension);
        if (loadedStem.generator() instanceof BCLChunkGenerator cg) {
            cg.restoreInitialBiomeSource();
        }
    }
}
