package org.betterx.worlds.together.world;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.worldPreset.WorldPresets;
import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.Optional;

public class WorldGenUtil {
    public static final String TAG_GENERATOR = "generator";

    public static WorldGenSettings createWorldFromPreset(
            ResourceKey<WorldPreset> preset,
            RegistryAccess registryAccess,
            long seed,
            boolean generateStructures,
            boolean generateBonusChest
    ) {
        WorldGenSettings settings = registryAccess
                .registryOrThrow(Registry.WORLD_PRESET_REGISTRY)
                .getHolderOrThrow(preset)
                .value()
                .createWorldGenSettings(seed, generateStructures, generateBonusChest);

        for (LevelStem stem : settings.dimensions()) {
            if (stem.generator().getBiomeSource() instanceof BiomeSourceWithSeed bcl) {
                bcl.setSeed(seed);
            }
        }

        return settings;
    }

    public static WorldGenSettings createDefaultWorldFromPreset(
            RegistryAccess registryAccess,
            long seed,
            boolean generateStructures,
            boolean generateBonusChest
    ) {
        return createWorldFromPreset(
                WorldPresets.DEFAULT.orElseThrow(),
                registryAccess,
                seed,
                generateStructures,
                generateBonusChest
        );
    }

    public static Pair<WorldGenSettings, RegistryAccess.Frozen> defaultWorldDataSupplier(RegistryAccess.Frozen frozen) {
        WorldGenSettings worldGenSettings = createDefaultWorldFromPreset(frozen);
        return Pair.of(worldGenSettings, frozen);
    }

    public static WorldGenSettings createDefaultWorldFromPreset(RegistryAccess registryAccess, long seed) {
        return createDefaultWorldFromPreset(registryAccess, seed, true, false);
    }

    public static WorldGenSettings createDefaultWorldFromPreset(RegistryAccess registryAccess) {
        return createDefaultWorldFromPreset(registryAccess, RandomSource.create().nextLong());
    }

    public static CompoundTag getSettingsNbt() {
        return WorldConfig.getCompoundTag(WorldsTogether.MOD_ID, TAG_GENERATOR);
    }

    public static WorldPresetSettings getWorldSettings() {
        if (BuiltinRegistries.ACCESS == null) return null;
        final RegistryAccess registryAccess = BuiltinRegistries.ACCESS;
        final RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryAccess);

        Optional<WorldPresetSettings> oLevelStem = WorldPresetSettings.CODEC
                .parse(new Dynamic<>(registryOps, getSettingsNbt()))
                .resultOrPartial(WorldsTogether.LOGGER::error);

        return oLevelStem.orElse(WorldPresetSettings.DEFAULT);
    }

    public static class Context extends StemContext {
        public final Registry<Biome> biomes;

        public Context(
                Registry<Biome> biomes, Holder<DimensionType> dimension,
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
}
