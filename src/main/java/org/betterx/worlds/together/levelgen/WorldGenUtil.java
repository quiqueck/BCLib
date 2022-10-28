package org.betterx.worlds.together.levelgen;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.biomesource.BiomeSourceWithConfig;
import org.betterx.worlds.together.biomesource.ReloadableBiomeSource;
import org.betterx.worlds.together.chunkgenerator.EnforceableChunkGenerator;
import org.betterx.worlds.together.world.BiomeSourceWithNoiseRelatedSettings;
import org.betterx.worlds.together.world.BiomeSourceWithSeed;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.event.WorldBootstrap;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

public class WorldGenUtil {
    public static final String TAG_PRESET = "preset";
    public static final String TAG_GENERATOR = "generator";

    public static WorldDimensions createWorldFromPreset(
            ResourceKey<WorldPreset> preset,
            RegistryAccess registryAccess,
            long seed,
            boolean generateStructures,
            boolean generateBonusChest
    ) {
        WorldDimensions settings = registryAccess
                .registryOrThrow(Registry.WORLD_PRESET_REGISTRY)
                .getHolderOrThrow(preset)
                .value()
                .createWorldDimensions();

        for (LevelStem stem : settings.dimensions()) {
            if (stem.generator().getBiomeSource() instanceof BiomeSourceWithSeed bcl) {
                bcl.setSeed(seed);
            }

            if (stem.generator().getBiomeSource() instanceof BiomeSourceWithNoiseRelatedSettings bcl
                    && stem.generator() instanceof NoiseBasedChunkGenerator noiseGenerator) {
                bcl.onLoadGeneratorSettings(noiseGenerator.generatorSettings().value());
            }
        }

        return settings;
    }

    public static WorldDimensions createDefaultWorldFromPreset(
            RegistryAccess registryAccess,
            long seed,
            boolean generateStructures,
            boolean generateBonusChest
    ) {
        return createWorldFromPreset(
                WorldPresets.getDEFAULT(),
                registryAccess,
                seed,
                generateStructures,
                generateBonusChest
        );
    }

    public static WorldDimensions createDefaultWorldFromPreset(RegistryAccess registryAccess, long seed) {
        return createDefaultWorldFromPreset(registryAccess, seed, true, false);
    }

    public static WorldDimensions createDefaultWorldFromPreset(RegistryAccess registryAccess) {
        return createDefaultWorldFromPreset(registryAccess, RandomSource.create().nextLong());
    }

    public static CompoundTag getPresetsNbt() {
        return WorldConfig.getCompoundTag(WorldsTogether.MOD_ID, TAG_PRESET);
    }

    public static CompoundTag getGeneratorNbt() {
        CompoundTag root = WorldConfig.getRootTag(WorldsTogether.MOD_ID);
        if (root.contains(TAG_GENERATOR))
            return WorldConfig.getCompoundTag(WorldsTogether.MOD_ID, TAG_GENERATOR);
        return null;
    }

    public static class Context extends StemContext {
        public final Registry<Biome> biomes;

        public Context(
                Registry<Biome> biomes,
                Holder<DimensionType> dimension,
                Registry<StructureSet> structureSets,
                Registry<NormalNoise.NoiseParameters> noiseParameters,
                Holder<NoiseGeneratorSettings> generatorSettings
        ) {
            super(dimension, structureSets, noiseParameters, generatorSettings);
            this.biomes = biomes;
        }
    }

    public static class StemContext {
        public final Holder<DimensionType> dimension;
        public final Registry<StructureSet> structureSets;
        public final Registry<NormalNoise.NoiseParameters> noiseParameters;
        public final Holder<NoiseGeneratorSettings> generatorSettings;

        public StemContext(
                Holder<DimensionType> dimension,
                Registry<StructureSet> structureSets,
                Registry<NormalNoise.NoiseParameters> noiseParameters,
                Holder<NoiseGeneratorSettings> generatorSettings
        ) {
            this.dimension = dimension;
            this.structureSets = structureSets;
            this.noiseParameters = noiseParameters;
            this.generatorSettings = generatorSettings;
        }
    }


    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public static Registry<LevelStem> repairBiomeSourceInAllDimensions(
            RegistryAccess registryAccess,
            Registry<LevelStem> dimensionRegistry
    ) {
        Map<ResourceKey<LevelStem>, ChunkGenerator> dimensions = TogetherWorldPreset.loadWorldDimensions();
        for (var entry : dimensionRegistry.entrySet()) {
            boolean didRepair = false;
            ResourceKey<LevelStem> key = entry.getKey();
            LevelStem loadedStem = entry.getValue();

            ChunkGenerator referenceGenerator = dimensions.get(key);
            if (referenceGenerator instanceof EnforceableChunkGenerator enforcer) {

//                // probably not a datapack, so we need to check what other mods would have
//                // added to the vanilla settings
//                if (loadedStem.generator() instanceof EnforceableChunkGenerator) {
//                    // This list contains the vanilla default level stem (only available if a new world is loaded) as well as
//                    // The currently loaded stem
//                    var vanillaDimensionMap = WorldGenUtil.getDimensionsWithModData(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);
//
//                    LevelStem vanillaDefaultStem = vanillaDimensionMap.get(key);
//                    if (vanillaDefaultStem != null) {
//                        loadedStem = vanillaDefaultStem;
//                    }
//                }


                // now compare the reference world settings (the ones that were created when the world was
                // started) with the settings that were loaded by the game.
                // If those do not match, we will create a new ChunkGenerator / BiomeSources with appropriate
                // settings

                final ChunkGenerator loadedChunkGenerator = loadedStem.generator();

                if (enforcer.togetherShouldRepair(loadedChunkGenerator)) {
                    dimensionRegistry = enforcer.enforceGeneratorInWorldGenSettings(
                            registryAccess,
                            key,
                            loadedStem.type().unwrapKey().orElseThrow(),
                            loadedChunkGenerator,
                            dimensionRegistry
                    );
                    didRepair = true;
                } else if (loadedChunkGenerator.getBiomeSource() instanceof BiomeSourceWithConfig bs) {
                    if (referenceGenerator.getBiomeSource() instanceof BiomeSourceWithConfig refbs) {
                        if (!refbs.getTogetherConfig().sameConfig(bs.getTogetherConfig())) {
                            bs.setTogetherConfig(refbs.getTogetherConfig());
                        }
                    }
                }
            }


            if (!didRepair) {
                if (loadedStem.generator().getBiomeSource() instanceof ReloadableBiomeSource reload) {
                    reload.reloadBiomes();
                }
            }

        }
        return dimensionRegistry;
    }

    public static ResourceLocation getBiomeID(Biome biome) {
        ResourceLocation id = null;
        RegistryAccess access = WorldBootstrap.getLastRegistryAccessOrElseBuiltin();

        id = access.registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);

        if (id == null) {
            WorldsTogether.LOGGER.error("Unable to get ID for " + biome + ".");
        }

        return id;
    }
}
