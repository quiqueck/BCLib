package org.betterx.bclib.api.v2.levelgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.WorldDataAPI;
import org.betterx.bclib.api.v2.generator.BCLBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLChunkGenerator;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.presets.worldgen.BCLWorldPreset;
import org.betterx.bclib.presets.worldgen.BCLWorldPresetSettings;
import org.betterx.bclib.presets.worldgen.BCLWorldPresets;
import org.betterx.bclib.presets.worldgen.WorldPresetSettings;
import org.betterx.bclib.util.ModUtil;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class LevelGenUtil {
    private static final String TAG_VERSION = "version";
    private static final String TAG_BN_GEN_VERSION = "generator_version";
    public static final String TAG_GENERATOR = "generator";

    @NotNull
    public static LevelStem getBCLNetherLevelStem(Context context, Optional<Integer> version) {
        BCLibNetherBiomeSource netherSource = new BCLibNetherBiomeSource(context.biomes, version);
        return getBCLNetherLevelStem(context, netherSource);
    }

    public static LevelStem getBCLNetherLevelStem(StemContext context, BiomeSource biomeSource) {
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
    public static LevelStem getBCLEndLevelStem(StemContext context, BiomeSource biomeSource) {
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

    public static LevelStem getBCLEndLevelStem(Context context, Optional<Integer> version) {
        BCLibEndBiomeSource endSource = new BCLibEndBiomeSource(context.biomes, version);
        return getBCLEndLevelStem(context, endSource);
    }

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
            if (stem.generator().getBiomeSource() instanceof BCLBiomeSource bcl) {
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
                BCLWorldPresets.DEFAULT.orElseThrow(),
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
            referenceSettings = createWorldFromPreset(
                    BCLWorldPresets.BCL_WORLD_17,
                    registryAccess,
                    seed,
                    generateStructures,
                    generateBonusChest
            );
        } else {
            referenceSettings = createDefaultWorldFromPreset(
                    registryAccess,
                    seed,
                    generateStructures,
                    generateBonusChest
            );
        }
        return referenceSettings.dimensions().getHolder(dimensionKey);
    }

    public static int getBiomeVersionForCurrentWorld(ResourceKey<LevelStem> key) {
        final CompoundTag settingsNbt = getSettingsNbt();
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

    static CompoundTag getSettingsNbt() {
        return WorldDataAPI.getCompoundTag(BCLib.TOGETHER_WORLDS, TAG_GENERATOR);
    }

    public static WorldPresetSettings getWorldSettings() {
        final RegistryAccess registryAccess = RegistryAccess.builtinCopy();
        final RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryAccess);

        Optional<WorldPresetSettings> oLevelStem = WorldPresetSettings.CODEC
                .parse(new Dynamic<>(registryOps, getSettingsNbt()))
                .resultOrPartial(BCLib.LOGGER::error);

        return oLevelStem.orElse(BCLWorldPresetSettings.DEFAULT);
    }

    public static void migrateGeneratorSettings() {
        final CompoundTag settingsNbt = getSettingsNbt();

        if (settingsNbt.size() == 0) {
            BCLib.LOGGER.info("Found World without generator Settings. Setting up data...");
            int biomeSourceVersion = BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION;

            final CompoundTag bclRoot = WorldDataAPI.getRootTag(BCLib.MOD_ID);

            String bclVersion = "0.0.0";
            if (bclRoot.contains(TAG_VERSION)) {
                bclVersion = bclRoot.getString(TAG_VERSION);
            }
            boolean isPre18 = !ModUtil.isLargerOrEqualVersion(bclVersion, "1.0.0");

            if (isPre18) {
                BCLib.LOGGER.info("World was create pre 1.18!");
                biomeSourceVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE;
            }

            if (WorldDataAPI.hasMod("betternether")) {
                BCLib.LOGGER.info("Found Data from BetterNether, using for migration.");
                final CompoundTag bnRoot = WorldDataAPI.getRootTag("betternether");
                biomeSourceVersion = "1.17".equals(bnRoot.getString(TAG_BN_GEN_VERSION))
                        ? BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE
                        : BCLBiomeSource.BIOME_SOURCE_VERSION_HEX;
            }

            BCLib.LOGGER.info("Set world to BiomeSource Version " + biomeSourceVersion);
            BCLWorldPreset.writeWorldPresetSettings(new BCLWorldPresetSettings(
                    biomeSourceVersion,
                    biomeSourceVersion,
                    true
            ));
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

}
