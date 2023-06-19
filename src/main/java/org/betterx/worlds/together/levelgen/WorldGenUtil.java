package org.betterx.worlds.together.levelgen;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.world.BiomeSourceWithNoiseRelatedSettings;
import org.betterx.worlds.together.world.BiomeSourceWithSeed;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.event.WorldBootstrap;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;

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
                .registryOrThrow(Registries.WORLD_PRESET)
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
        public Context(
                Holder<DimensionType> dimension,
                HolderGetter<StructureSet> structureSets,
                Holder<NoiseGeneratorSettings> generatorSettings
        ) {
            super(dimension, structureSets, generatorSettings);
        }
    }

    public static class StemContext {
        public final Holder<DimensionType> dimension;
        public final HolderGetter<StructureSet> structureSets;
        public final Holder<NoiseGeneratorSettings> generatorSettings;

        public StemContext(
                Holder<DimensionType> dimension,
                HolderGetter<StructureSet> structureSets,
                Holder<NoiseGeneratorSettings> generatorSettings
        ) {
            this.dimension = dimension;
            this.structureSets = structureSets;
            this.generatorSettings = generatorSettings;
        }
    }


    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public static Registry<LevelStem> repairBiomeSourceInAllDimensions(
            RegistryAccess registryAccess,
            Registry<LevelStem> dimensionRegistry
    ) {
        return new BiomeRepairHelper().repairBiomeSourceInAllDimensions(registryAccess, dimensionRegistry);
    }

    public static ResourceLocation getBiomeID(Biome biome) {
        ResourceLocation id = null;
        RegistryAccess access = WorldBootstrap.getLastRegistryAccessOrElseBuiltin();

        id = access.registryOrThrow(Registries.BIOME).getKey(biome);

        if (id == null) {
            WorldsTogether.LOGGER.error("Unable to get ID for " + biome + ".");
        }

        return id;
    }
}
