package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.api.v2.levelgen.surface.SurfaceRuleUtil;
import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;
import org.betterx.bclib.interfaces.SurfaceRuleProvider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.List;
import java.util.Optional;
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


    public BCLChunkGenerator(
            Registry<StructureSet> registry,
            Registry<NormalNoise.NoiseParameters> registry2,
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> holder
    ) {
        super(registry, registry2, biomeSource, holder);
        System.out.println("Chunk Generator: " + this);
    }

    public static void injectNoiseSettings(WorldGenSettings settings) {
        BCLChunkGenerator.injectNoiseSettings(settings, BCLChunkGenerator.ALL_DIMENSIONS);
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
    public static final Predicate<ResourceKey<LevelStem>> ALL_DIMENSIONS = dim -> true;

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
}
