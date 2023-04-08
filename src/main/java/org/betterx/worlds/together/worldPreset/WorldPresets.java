package org.betterx.worlds.together.worldPreset;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.worlds.together.levelgen.WorldGenUtil;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import org.jetbrains.annotations.ApiStatus;

public class WorldPresets {
    private static ResourceKey<WorldPreset> DEFAULT = net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL;

    public static Holder<WorldPreset> get(RegistryAccess access, ResourceKey<WorldPreset> key) {
        return access
                .registryOrThrow(Registries.WORLD_PRESET)
                .getHolderOrThrow(key);
    }

    public static void ensureStaticallyLoaded() {

    }

    public static ResourceKey<WorldPreset> createKey(ResourceLocation loc) {
        return ResourceKey.create(Registries.WORLD_PRESET, loc);
    }

    public static ResourceKey<WorldPreset> getDEFAULT() {
        return DEFAULT;
    }


    @ApiStatus.Internal
    public static void setDEFAULT(ResourceKey<WorldPreset> DEFAULT) {
        WorldPresets.DEFAULT = DEFAULT;
    }

    public static class BootstrapData {
        public final HolderGetter<NoiseGeneratorSettings> noiseSettings;
        public final HolderGetter<Biome> biomes;
        public final HolderGetter<PlacedFeature> placedFeatures;
        public final HolderGetter<StructureSet> structureSets;
        public final LevelStem netherStem;
        public final LevelStem endStem;
        public final LevelStem overworldStem;
        public final Holder<DimensionType> netherDimensionType;
        public final Holder<DimensionType> endDimensionType;
        public final Holder<DimensionType> overworldDimensionType;

        public final WorldGenUtil.Context netherContext;
        public final WorldGenUtil.Context endContext;

        public final HolderGetter<MultiNoiseBiomeSourceParameterList> parameterLists;

        public BootstrapData(BootstapContext<WorldPreset> bootstapContext) {
            this.parameterLists = bootstapContext.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            final HolderGetter<DimensionType> dimensionTypes = bootstapContext.lookup(Registries.DIMENSION_TYPE);

            this.noiseSettings = bootstapContext.lookup(Registries.NOISE_SETTINGS);
            this.biomes = bootstapContext.lookup(Registries.BIOME);
            this.placedFeatures = bootstapContext.lookup(Registries.PLACED_FEATURE);
            this.structureSets = bootstapContext.lookup(Registries.STRUCTURE_SET);

            this.overworldDimensionType = dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
            Holder.Reference<MultiNoiseBiomeSourceParameterList> overworldParameters = parameterLists
                    .getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
            MultiNoiseBiomeSource overworldBiomeSource = MultiNoiseBiomeSource.createFromPreset(overworldParameters);
            Holder<NoiseGeneratorSettings> defaultOverworldNoise = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            this.overworldStem = makeNoiseBasedOverworld(overworldBiomeSource, defaultOverworldNoise);

            this.netherDimensionType = dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER);
            Holder.Reference<MultiNoiseBiomeSourceParameterList> netherParameters = parameterLists
                    .getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
            Holder<NoiseGeneratorSettings> defaultNetherNoise = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
            this.netherStem = new LevelStem(
                    netherDimensionType,
                    new NoiseBasedChunkGenerator(
                            MultiNoiseBiomeSource.createFromPreset(netherParameters),
                            defaultNetherNoise
                    )
            );

            this.endDimensionType = dimensionTypes.getOrThrow(BuiltinDimensionTypes.END);
            Holder<NoiseGeneratorSettings> defaultEndNoise = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.END);
            this.endStem = new LevelStem(
                    endDimensionType,
                    new NoiseBasedChunkGenerator(TheEndBiomeSource.create(this.biomes), defaultEndNoise)
            );


            Holder<NoiseGeneratorSettings> netherSettings, endSettings;
            if (this.netherStem.generator() instanceof NoiseBasedChunkGenerator nether) {
                netherSettings = nether.generatorSettings();
            } else {
                netherSettings = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
            }

            if (this.endStem.generator() instanceof NoiseBasedChunkGenerator nether) {
                endSettings = nether.generatorSettings();
            } else {
                endSettings = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.END);
            }

            HolderGetter<BCLBiome> bclBiomes = bootstapContext.lookup(BCLBiomeRegistry.BCL_BIOMES_REGISTRY);
            this.netherContext = new WorldGenUtil.Context(
                    this.netherStem.type(),
                    this.structureSets,
                    netherSettings
            );

            this.endContext = new WorldGenUtil.Context(
                    this.endStem.type(),
                    this.structureSets,
                    endSettings
            );
        }

        private LevelStem makeOverworld(ChunkGenerator chunkGenerator) {
            return new LevelStem(this.overworldDimensionType, chunkGenerator);
        }

        public LevelStem makeNoiseBasedOverworld(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
            return this.makeOverworld(new NoiseBasedChunkGenerator(biomeSource, holder));
        }
    }
}
