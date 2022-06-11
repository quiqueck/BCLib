package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.BiomeSourceAccessor;
import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;
import org.betterx.bclib.interfaces.SurfaceRuleProvider;
import org.betterx.bclib.mixin.common.BiomeGenerationSettingsAccessor;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class InternalBiomeAPI {
    static final Map<Biome, BCLBiome> CLIENT = Maps.newHashMap();
    static final Map<Holder<PlacedFeature>, Integer> FEATURE_ORDER = Maps.newHashMap();
    static final MutableInt FEATURE_ORDER_ID = new MutableInt(0);
    static final Map<ResourceKey<LevelStem>, List<BiConsumer<ResourceLocation, Holder<Biome>>>> MODIFICATIONS = Maps.newHashMap();
    static final Map<ResourceKey, List<BiConsumer<ResourceLocation, Holder<Biome>>>> TAG_ADDERS = Maps.newHashMap();
    static Registry<Biome> biomeRegistry;
    static RegistryAccess registryAccess;

    static void initFeatureOrder() {
        if (!FEATURE_ORDER.isEmpty()) {
            return;
        }

        BuiltinRegistries.BIOME
                .entrySet()
                .stream()
                .filter(entry -> entry
                        .getKey()
                        .location()
                        .getNamespace()
                        .equals("minecraft"))
                .map(Map.Entry::getValue)
                .map(biome -> (BiomeGenerationSettingsAccessor) biome.getGenerationSettings())
                .map(BiomeGenerationSettingsAccessor::bclib_getFeatures)
                .forEach(stepFeatureSuppliers -> stepFeatureSuppliers.forEach(step -> step.forEach(feature -> {
                    FEATURE_ORDER.computeIfAbsent(feature, f -> FEATURE_ORDER_ID.getAndIncrement());
                })));
    }

    /**
     * Initialize registry for current server.
     *
     * @param access - The new, active {@link RegistryAccess} for the current session.
     */
    public static void initRegistry(RegistryAccess access) {
        if (access != registryAccess) {
            registryAccess = access;
            Registry<Biome> biomeRegistry = access.registry(Registry.BIOME_REGISTRY).orElse(null);

            if (biomeRegistry != InternalBiomeAPI.biomeRegistry) {
                InternalBiomeAPI.biomeRegistry = biomeRegistry;
                CLIENT.clear();

                BIOMES_TO_SORT.forEach(id -> {
                    Biome b = biomeRegistry.get(id);
                    if (b != null) {
                        BCLib.LOGGER.info("Sorting Features in Biome: " + id + "(" + b + ")");
                        BiomeAPI.sortBiomeFeatures(b);
                    } else {
                        BCLib.LOGGER.info("Unknown Biome: " + id);
                    }
                });
            }
        }
    }

    /**
     * For internal use only.
     * <p>
     * This method gets called before a world is loaded/created to flush cashes we build.
     */
    public static void prepareNewLevel() {
        BIOMES_TO_SORT.clear();
    }

    /**
     * Load biomes from Fabric API. For internal usage only.
     */
    public static void loadFabricAPIBiomes() {
        FabricBiomesData.NETHER_BIOMES.forEach((key) -> {
            if (!BiomeAPI.hasBiome(key.location())) {
                Optional<Holder<Biome>> optional = BuiltinRegistries.BIOME.getHolder(key);
                if (optional.isPresent()) {
                    BiomeAPI.registerNetherBiome(optional.get().value());
                }
            }
        });

        FabricBiomesData.END_LAND_BIOMES.forEach((key, weight) -> {
            if (!BiomeAPI.hasBiome(key.location())) {
                Optional<Holder<Biome>> optional = BuiltinRegistries.BIOME.getHolder(key);
                if (optional.isPresent()) {
                    BiomeAPI.registerEndLandBiome(optional.get(), weight);
                }
            }
        });

        FabricBiomesData.END_VOID_BIOMES.forEach((key, weight) -> {
            if (!BiomeAPI.hasBiome(key.location())) {
                Optional<Holder<Biome>> optional = BuiltinRegistries.BIOME.getHolder(key);
                if (optional.isPresent()) {
                    BiomeAPI.registerEndVoidBiome(optional.get(), weight);
                }
            }
        });
    }

    /**
     * For internal use only
     */
    public static void _runBiomeTagAdders() {
        for (var mod : TAG_ADDERS.entrySet()) {
            Stream<ResourceLocation> s = null;
            if (mod.getKey() == Level.NETHER) s = BiomeAPI.BiomeType.BIOME_TYPE_MAP.entrySet()
                                                                                   .stream()
                                                                                   .filter(e -> e.getValue()
                                                                                                 .is(BiomeAPI.BiomeType.NETHER))
                                                                                   .map(e -> e.getKey());
            else if (mod.getKey() == Level.END) s = BiomeAPI.BiomeType.BIOME_TYPE_MAP.entrySet()
                                                                                     .stream()
                                                                                     .filter(e -> e.getValue().is(
                                                                                             BiomeAPI.BiomeType.END))
                                                                                     .map(e -> e.getKey());
            if (s != null) {
                s.forEach(id -> {
                    Holder<Biome> biomeHolder = BiomeAPI.getBiomeHolder(id);
                    if (biomeHolder.isBound()) {
                        mod.getValue().forEach(c -> c.accept(id, biomeHolder));
                    }
                });
            }
        }
    }

    /**
     * Will apply biome modifications to world, internal usage only.
     *
     * @param level
     */
    @Deprecated(forRemoval = true)
    public static void applyModificationsDeprecated(ServerLevel level) {
        //TODO: Now Disabled, because we fix the settings when everything gets loaded
        if (level != null) return;

        NoiseGeneratorSettings noiseGeneratorSettings = null;
        final ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        final BiomeSource source = chunkGenerator.getBiomeSource();
        final Set<Holder<Biome>> biomes = source.possibleBiomes();

        if (chunkGenerator instanceof NoiseGeneratorSettingsProvider gen)
            noiseGeneratorSettings = gen.bclib_getNoiseGeneratorSettings();

        // Datapacks (like Amplified Nether)will change the GeneratorSettings upon load, so we will
        // only use the default Setting for Nether/End if we were unable to find a settings object
        if (noiseGeneratorSettings == null) {
            if (level.dimension() == Level.NETHER) {
                noiseGeneratorSettings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.NETHER);
            } else if (level.dimension() == Level.END) {
                noiseGeneratorSettings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.END);
            }
        }

        List<BiConsumer<ResourceLocation, Holder<Biome>>> modifications = MODIFICATIONS.get(level
                .dimensionTypeRegistration()
                .unwrapKey()
                .orElseThrow());
        for (Holder<Biome> biomeHolder : biomes) {
            if (biomeHolder.isBound()) {
                applyModificationsAndUpdateFeatures(modifications, biomeHolder);
            }
        }


        if (noiseGeneratorSettings != null) {
            final SurfaceRuleProvider provider = SurfaceRuleProvider.class.cast(noiseGeneratorSettings);
            // Multiple Biomes can use the same generator. So we need to keep track of all Biomes that are
            // Provided by all the BiomeSources that use the same generator.
            // This happens for example when using the MiningDimensions, which reuses the generator for the
            // Nethering Dimension
            //MODIFIED_SURFACE_PROVIDERS.add(provider);
            provider.bclib_addBiomeSource(source);
        } else {
            BCLib.LOGGER.warning("No generator for " + source);
        }

        ((BiomeSourceAccessor) source).bclRebuildFeatures();
    }

    public static void applyModifications(BiomeSource source, ResourceKey<LevelStem> dimension) {
        BCLib.LOGGER.info("Apply Modifications for " + dimension.location() + " BiomeSource " + source);
        /*if (dimension.location().equals(LevelStem.NETHER)){
            if (source instanceof BCLBiomeSource s) {
                NetherBiomes.useLegacyGeneration = s.biomeSourceVersion==BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE;
            }
        }*/
        final Set<Holder<Biome>> biomes = source.possibleBiomes();
        List<BiConsumer<ResourceLocation, Holder<Biome>>> modifications = MODIFICATIONS.get(dimension);
        for (Holder<Biome> biomeHolder : biomes) {
            if (biomeHolder.isBound()) {
                applyModificationsAndUpdateFeatures(modifications, biomeHolder);
            }
        }
    }

    private static void applyModificationsAndUpdateFeatures(
            List<BiConsumer<ResourceLocation, Holder<Biome>>> modifications,
            Holder<Biome> biome
    ) {
        ResourceLocation biomeID = BiomeAPI.getBiomeID(biome);
        if (modifications != null) {
            modifications.forEach(consumer -> {
                consumer.accept(biomeID, biome);
            });
        }

        BiomeAPI.sortBiomeFeatures(biome);
    }

    private static final Set<ResourceLocation> BIOMES_TO_SORT = Sets.newHashSet();

    static {
        DynamicRegistrySetupCallback.EVENT.register(registryManager -> {
            Optional<? extends Registry<Biome>> oBiomeRegistry = registryManager.registry(Registry.BIOME_REGISTRY);
            RegistryEntryAddedCallback
                    .event(oBiomeRegistry.get())
                    .register((rawId, id, biome) -> {
                        BCLBiome b = BiomeAPI.getBiome(id);
                        if (!"minecraft".equals(id.getNamespace()) && (b == null || b == BiomeAPI.EMPTY_BIOME)) {
                            BCLib.LOGGER.info(" #### " + rawId + ", " + biome + ", " + id);
                            BIOMES_TO_SORT.add(id);
                        }
                    });
        });
    }
}
