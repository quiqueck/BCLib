package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class InternalBiomeAPI {
    public static final BiomeAPI.BiomeType OTHER_NETHER = new BiomeAPI.BiomeType(
            "OTHER_NETHER",
            BiomeAPI.BiomeType.NETHER
    );
    public static final BiomeAPI.BiomeType OTHER_END_LAND = new BiomeAPI.BiomeType(
            "OTHER_END_LAND",
            BiomeAPI.BiomeType.END_LAND
    );
    public static final BiomeAPI.BiomeType OTHER_END_VOID = new BiomeAPI.BiomeType(
            "OTHER_END_VOID",
            BiomeAPI.BiomeType.END_VOID
    );
    public static final BiomeAPI.BiomeType OTHER_END_CENTER = new BiomeAPI.BiomeType(
            "OTHER_END_CENTER",
            BiomeAPI.BiomeType.END_CENTER
    );
    public static final BiomeAPI.BiomeType OTHER_END_BARRENS = new BiomeAPI.BiomeType(
            "OTHER_END_BARRENS",
            BiomeAPI.BiomeType.END_BARRENS
    );
    static final Map<Biome, BCLBiome> CLIENT = Maps.newHashMap();
    static final Map<Holder<PlacedFeature>, Integer> FEATURE_ORDER = Maps.newHashMap();

    static final Map<HolderGetter<Biome>, AtomicInteger> BIOME_ADDITIONS = Maps.newHashMap();
    static final MutableInt FEATURE_ORDER_ID = new MutableInt(0);
    static final Map<ResourceKey<LevelStem>, List<BiConsumer<ResourceLocation, Holder<Biome>>>> MODIFICATIONS = Maps.newHashMap();
    static final Map<ResourceKey, List<BiConsumer<ResourceLocation, Holder<Biome>>>> TAG_ADDERS = Maps.newHashMap();
    static Registry<Biome> biomeRegistry;
    static RegistryAccess registryAccess;

    public static RegistryAccess worldRegistryAccess() {
        return registryAccess;
    }

    /**
     * Initialize registry for current server.
     *
     * @param access - The new, active {@link RegistryAccess} for the current session.
     */
    public static void initRegistry(RegistryAccess access) {
        if (access != registryAccess) {
            registryAccess = access;
            Registry<Biome> biomeRegistry = access.registry(Registries.BIOME).orElse(null);

            if (biomeRegistry != InternalBiomeAPI.biomeRegistry) {
                InternalBiomeAPI.biomeRegistry = biomeRegistry;
                CLIENT.clear();

                BIOMES_TO_SORT.forEach(id -> {
                    Biome b = biomeRegistry.get(id);
                    if (b != null) {
//                        BCLib.LOGGER.info("Found non fabric/bclib Biome: " + id + "(" + b + ")");
//                        BiomeAPI.sortBiomeFeatures(b);
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
//        FabricBiomesData.NETHER_BIOMES.forEach((key) -> {
//            if (!BiomeAPI.hasBiome(key.location())) {
//                Optional<Holder<Biome>> optional = BuiltinRegistries.BIOME.getHolder(key);
//                if (optional.isPresent()) {
//                    BiomeAPI.registerNetherBiome(optional.get().value());
//                }
//            }
//        });
//
//        FabricBiomesData.END_LAND_BIOMES.forEach((key, weight) -> {
//            if (!BiomeAPI.hasBiome(key.location())) {
//                Optional<Holder<Biome>> optional = BuiltinRegistries.BIOME.getHolder(key);
//                if (optional.isPresent()) {
//                    BiomeAPI.registerEndLandBiome(optional.get(), weight);
//                }
//            }
//        });
//
//        FabricBiomesData.END_VOID_BIOMES.forEach((key, weight) -> {
//            if (!BiomeAPI.hasBiome(key.location())) {
//                Optional<Holder<Biome>> optional = BuiltinRegistries.BIOME.getHolder(key);
//                if (optional.isPresent()) {
//                    BiomeAPI.registerEndVoidBiome(optional.get(), weight);
//                }
//            }
//        });
    }

    /**
     * For internal use only
     */
    public static void _runBiomeTagAdders() {
        for (var mod : TAG_ADDERS.entrySet()) {
            Stream<ResourceLocation> s = null;
            if (mod.getKey() == Level.NETHER)
                s = BCLBiomeRegistry.getAll(BiomeAPI.BiomeType.NETHER).map(k -> k.location());
            else if (mod.getKey() == Level.END)
                s = BCLBiomeRegistry.getAll(BiomeAPI.BiomeType.END).map(k -> k.location());
            if (s != null) {
                s.forEach(id -> {
                    Holder<Biome> biomeHolder = BiomeAPI.getFromRegistry(id);
                    if (biomeHolder != null && biomeHolder.isBound()) {
                        mod.getValue().forEach(c -> c.accept(id, biomeHolder));
                    } else {
                        BCLib.LOGGER.info("No Holder for " + id);
                    }
                });
            }
        }
    }

    public static void applyModifications(BiomeSource source, ResourceKey<LevelStem> dimension) {
        if (Configs.MAIN_CONFIG.verboseLogging())
            BCLib.LOGGER.info("\nApply Modifications for " + dimension.location() + source.toString()
                                                                                          .replace("\n", "\n    "));

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
    }

    private static final Set<ResourceLocation> BIOMES_TO_SORT = Sets.newHashSet();

    /**
     * Register {@link BCLBiome} wrapper for {@link Biome}.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
     *
     * @param biomeKey The source biome to wrap
     * @return {@link BCLBiome}
     */
    public static BCLBiome wrapBiome(ResourceKey<Biome> biomeKey, BiomeAPI.BiomeType type) {
        return wrapBiome(biomeKey, -1, type);
    }

    /**
     * Register {@link BCLBiome} wrapper for {@link Biome}.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
     *
     * @param biomeKey  The source biome to wrap
     * @param genChance generation chance. If &lt;0 the default genChance is used
     * @return {@link BCLBiome}
     */
    public static BCLBiome wrapBiome(ResourceKey<Biome> biomeKey, float genChance, BiomeAPI.BiomeType type) {
        return wrapBiome(
                biomeKey,
                genChance < 0 ? null : VanillaBiomeSettings.createVanilla().setGenChance(genChance).build(),
                type
        );
    }

    public static BCLBiome wrapBiome(
            ResourceKey<Biome> biomeKey,
            BCLBiome edgeBiome,
            int edgeBiomeSize,
            float genChance,
            BiomeAPI.BiomeType type
    ) {
        VanillaBiomeSettings.Builder settings = VanillaBiomeSettings.createVanilla();
        if (genChance >= 0) settings.setGenChance(genChance);
        settings.setEdgeSize(edgeBiomeSize);

        final BCLBiome b = wrapBiome(biomeKey, settings.build(), type);
        b._setEdge(edgeBiome);
        return b;
    }

    /**
     * Create a wrapper for a vanilla {@link Biome}.
     *
     * @param biomeKey The source biome to wrap
     * @param setings  the {@link VanillaBiomeSettings} to use
     * @return {@link BCLBiome}
     */
    private static BCLBiome wrapBiome(
            ResourceKey<Biome> biomeKey,
            VanillaBiomeSettings setings,
            BiomeAPI.BiomeType type
    ) {
        final Registry<BCLBiome> reg = BCLBiomeRegistry.registryOrNull();
        if (BCLBiomeRegistry.hasBiome(biomeKey, reg)) {
            return BCLBiomeRegistry.getBiome(biomeKey, reg);
        }

        BCLBiome bclBiome = new BCLBiome(biomeKey, setings);
        bclBiome._setIntendedType(type);

        registerBuiltinBiome(bclBiome);
        return bclBiome;
    }


    /**
     * Register {@link BCLBiome} wrapper for {@link Biome}.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
     *
     * @param biomeKey The source biome to wrap
     * @return {@link BCLBiome}
     */
    public static BCLBiome wrapNativeBiome(ResourceKey<Biome> biomeKey, BiomeAPI.BiomeType type) {
        final Registry<BCLBiome> reg = BCLBiomeRegistry.registryOrNull();
        if (!BCLBiomeRegistry.hasBiome(biomeKey, reg)) {
            BCLBiome bclBiome = wrapBiome(biomeKey, type);
            BCLBiomeRegistry.register(bclBiome);
            registerBuiltinBiome(bclBiome);
            return bclBiome;
        } else {
            return BCLBiomeRegistry.getBiome(biomeKey, reg);
        }


    }

    static {
        DynamicRegistrySetupCallback.EVENT.register(registryManager -> {
            Optional<? extends Registry<Biome>> oBiomeRegistry = registryManager.asDynamicRegistryManager()
                                                                                .registry(Registries.BIOME);
            if (oBiomeRegistry.isPresent()) {
                RegistryEntryAddedCallback
                        .event(oBiomeRegistry.get())
                        .register((rawId, id, biome) -> {
                            BCLBiome b = BiomeAPI.getBiome(id);
                            if (!"minecraft".equals(id.getNamespace()) && BCLBiomeRegistry.isEmptyBiome(b)) {
                                //BCLib.LOGGER.info(" #### " + rawId + ", " + biome + ", " + id);
                                //BIOMES_TO_SORT.add(id);
//                            BIOME_ADDITIONS.computeIfAbsent(oBiomeRegistry.get(), reg -> new AtomicInteger(0))
//                                           .incrementAndGet();
                            }
                        });
            } else {
                BCLib.LOGGER.warning("No valid Biome Registry available!");
            }
        });

    }

    /**
     * The BCLBiomeSource keeps track of Modifications that happen after the BiomeSource was initialized.
     * This appears to happen especially for new Worlds where the Biome Source is deserialized
     * when the WolrdPreset registry is built for the CreateScreen. However Farbic Biomes are not yet
     * added to the biomeRegistry at this stage.
     * The counter is incremented in the DynamicRegistrySetupCallback.EVENT for the Biome Registry
     *
     * @param registry The registry you want to check
     * @return The current number of additions since the world creation was started
     */
    public static int getBiomeRegistryModificationCount(HolderGetter<Biome> registry) {
        if (registry == null) return 0;
        return BIOME_ADDITIONS.computeIfAbsent(registry, reg -> new AtomicInteger(0)).get();
    }

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     *
     * @param bclbiome {@link BCLBiome}
     * @return {@link BCLBiome}
     */

    public static BCLBiome registerBuiltinBiome(BCLBiome bclbiome) {
        return BiomeAPI.finishBiomeRegistration(bclbiome);
    }

}
