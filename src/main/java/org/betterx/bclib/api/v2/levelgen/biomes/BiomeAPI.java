package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.features.BCLFeature;
import org.betterx.bclib.interfaces.SurfaceMaterialProvider;
import org.betterx.bclib.mixin.common.BiomeGenerationSettingsAccessor;
import org.betterx.bclib.mixin.common.MobSpawnSettingsAccessor;
import org.betterx.bclib.util.CollectionsUtil;
import org.betterx.worlds.together.tag.v3.CommonBiomeTags;
import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import net.fabricmc.fabric.api.biome.v1.NetherBiomes;
import net.fabricmc.fabric.api.biome.v1.TheEndBiomes;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BiomeAPI {
    public static class BiomeType {
        public static final Codec<BiomeType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        Codec.STRING.fieldOf("name")
                                    .orElse("undefined")
                                    .forGetter(o -> o.name)

                ).apply(instance, BiomeType::create));
        public static final Codec<BiomeType> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        Codec.STRING.fieldOf("name")
                                    .orElse("undefined")
                                    .forGetter(o -> o.name),
                        Codec.STRING.fieldOf("parent")
                                    .orElse("none")
                                    .forGetter(o -> o.parentOrNull == null ? "none" : o.parentOrNull.name)

                ).apply(instance, BiomeType::create));

        private static final Map<String, BiomeType> KNOWN_TYPES = new HashMap<>();

        public static final BiomeType NONE = new BiomeType("NONE");
        public static final BiomeType OVERWORLD = new BiomeType("OVERWORLD");
        public static final BiomeType NETHER = new BiomeType("NETHER");
        public static final BiomeType BCL_NETHER = new BiomeType("BCL_NETHER", NETHER, (biome, ignored) -> {
            ResourceKey<Biome> key = biome.getBiomeKey();
            if (!biome.isEdgeBiome()) {
                biome.forEachClimateParameter(p -> NetherBiomes.addNetherBiome(key, p));
            }
        });
        public static final BiomeType END = new BiomeType("END");
        public static final BiomeType END_IGNORE = new BiomeType("END_IGNORE", END);
        public static final BiomeType END_LAND = new BiomeType("END_LAND", END);
        public static final BiomeType END_VOID = new BiomeType("END_VOID", END);
        public static final BiomeType END_CENTER = new BiomeType("END_CENTER", END);
        public static final BiomeType END_BARRENS = new BiomeType("END_BARRENS", END);
        public static final BiomeType BCL_END_LAND = new BiomeType("BCL_END_LAND", END_LAND, (biome, ignored) -> {
            float weight = biome.getGenChance();
            ResourceKey<Biome> key = biome.getBiomeKey();

            if (biome.isEdgeBiome()) {
                ResourceKey<Biome> parentKey = biome.getParentBiome().getBiomeKey();
                TheEndBiomes.addMidlandsBiome(parentKey, key, weight);
            } else {
                TheEndBiomes.addHighlandsBiome(key, weight);
            }
        });
        public static final BiomeType BCL_END_VOID = new BiomeType("BCL_END_VOID", END_VOID, (biome, ignored) -> {
            float weight = biome.getGenChance();
            ResourceKey<Biome> key = biome.getBiomeKey();
            if (!biome.isEdgeBiome()) {
                TheEndBiomes.addSmallIslandsBiome(key, weight);
            }
        });
        public static final BiomeType BCL_END_CENTER = new BiomeType("BCL_END_CENTER", END_CENTER, (biome, ignored) -> {
            float weight = biome.getGenChance();
            ResourceKey<Biome> key = biome.getBiomeKey();
            if (!biome.isEdgeBiome()) {
                TheEndBiomes.addMainIslandBiome(key, weight);
            }
        });

        public static final BiomeType BCL_END_BARRENS = new BiomeType(
                "BCL_END_BARRENS",
                END_BARRENS,
                (biome, highlandBiome) -> {
                    float weight = biome.getGenChance();
                    ResourceKey<Biome> key = biome.getBiomeKey();
                    if (!biome.isEdgeBiome()) {
                        ResourceKey<Biome> parentKey = highlandBiome.getBiomeKey();
                        TheEndBiomes.addBarrensBiome(parentKey, key, weight);
                    }
                }
        );

        public final BiomeType parentOrNull;
        private final String name;

        @FunctionalInterface
        interface ExtraRegisterTaks {
            void register(@NotNull BCLBiome biome, @Nullable BCLBiome parent);
        }

        final ExtraRegisterTaks extraRegisterTask;

        private static BiomeType create(String name, String parentOrNull) {
            BiomeType known = KNOWN_TYPES.get(name);
            BiomeType parent = parentOrNull == null || "none".equals(parentOrNull)
                    ? null
                    : KNOWN_TYPES.get(parentOrNull);
            if (known != null) {
                if (known.parentOrNull != parent) {
                    BCLib.LOGGER.warning("BiomeType " + name + " was deserialized with parent " + parent + " but already has " + known.parentOrNull);
                }
                return known;
            }
            return new BiomeType(name, parent);
        }

        static BiomeType create(String name) {
            BiomeType known = KNOWN_TYPES.get(name);
            if (known != null) {
                return known;
            }
            return NONE;
        }

        public BiomeType(String name) {
            this(name, null);
        }

        public BiomeType(String name, BiomeType parentOrNull) {
            this(name, parentOrNull, (b, a) -> {
            });
        }

        public BiomeType(String name, BiomeType parentOrNull, ExtraRegisterTaks extraRegisterTask) {
            this.parentOrNull = parentOrNull;
            this.name = name;
            this.extraRegisterTask = extraRegisterTask;
            KNOWN_TYPES.put(name, this);
        }

        public boolean is(BiomeType d) {
            if (d == this) return true;
            if (parentOrNull != null) return parentOrNull.is(d);
            return false;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            String str = name;
            if (parentOrNull != null) str += " -> " + parentOrNull;
            return str;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BiomeType biomeType = (BiomeType) o;
            return name.equals(biomeType.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    /**
     * Empty biome used as default value if requested biome doesn't exist or linked. Shouldn't be registered anywhere to prevent bugs.
     * Have {@code Biomes.THE_VOID} as the reference biome.
     *
     * @deprecated use {@link BCLBiomeRegistry#EMPTY_BIOME} instead
     */
    public static final BCLBiome EMPTY_BIOME = BCLBiomeRegistry.EMPTY_BIOME;

    public static final BCLBiome NETHER_WASTES_BIOME = InternalBiomeAPI.wrapNativeBiome(
            Biomes.NETHER_WASTES,
            InternalBiomeAPI.OTHER_NETHER
    );
    public static final BCLBiome CRIMSON_FOREST_BIOME = InternalBiomeAPI.wrapNativeBiome(
            Biomes.CRIMSON_FOREST,
            InternalBiomeAPI.OTHER_NETHER
    );
    public static final BCLBiome WARPED_FOREST_BIOME = InternalBiomeAPI.wrapNativeBiome(
            Biomes.WARPED_FOREST,
            InternalBiomeAPI.OTHER_NETHER
    );
    public static final BCLBiome SOUL_SAND_VALLEY_BIOME = InternalBiomeAPI.wrapNativeBiome(
            Biomes.SOUL_SAND_VALLEY,
            InternalBiomeAPI.OTHER_NETHER
    );
    public static final BCLBiome BASALT_DELTAS_BIOME = InternalBiomeAPI.wrapNativeBiome(
            Biomes.BASALT_DELTAS,
            InternalBiomeAPI.OTHER_NETHER
    );


    public static final BCLBiome THE_END = InternalBiomeAPI.wrapNativeBiome(
            Biomes.THE_END,
            0.5F,
            InternalBiomeAPI.OTHER_END_CENTER
    );

    public static final BCLBiome END_MIDLANDS = InternalBiomeAPI.wrapNativeBiome(
            Biomes.END_MIDLANDS,
            0.5F,
            InternalBiomeAPI.OTHER_END_LAND
    );

    public static final BCLBiome END_HIGHLANDS = InternalBiomeAPI.wrapNativeBiome(
            Biomes.END_HIGHLANDS,
            END_MIDLANDS,
            8,
            0.5F,
            InternalBiomeAPI.OTHER_END_LAND
    );


    public static final BCLBiome END_BARRENS = InternalBiomeAPI.wrapNativeBiome(
            Biomes.END_BARRENS,
            InternalBiomeAPI.OTHER_END_BARRENS
    );

    public static final BCLBiome SMALL_END_ISLANDS = InternalBiomeAPI.wrapNativeBiome(
            Biomes.SMALL_END_ISLANDS,
            InternalBiomeAPI.OTHER_END_VOID
    );

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     *
     * @param bclbiome {@link BCLBiome}
     * @return {@link BCLBiome}
     */
    public static BCLBiome registerBiome(BCLBiome bclbiome) {
        return registerBiome(bclbiome, BuiltinRegistries.BIOME);
    }

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     *
     * @param bclbiome {@link BCLBiome}
     * @param dim      The Dimension fo rthis Biome
     * @return {@link BCLBiome}
     */
    @Deprecated(forRemoval = true)
    public static BCLBiome registerBiome(BCLBiome bclbiome, BiomeType dim) {
        return registerBiome(bclbiome, dim, BuiltinRegistries.BIOME);
    }

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     *
     * @param bclbiome {@link BCLBiome}
     * @param dim      The Dimension fo rthis Biome
     * @return {@link BCLBiome}
     */
    @Deprecated(forRemoval = true)
    static BCLBiome registerBiome(BCLBiome bclbiome, BiomeType dim, Registry<Biome> registryOrNull) {
        bclbiome._setIntendedType(dim);
        return registerBiome(bclbiome, registryOrNull);
    }

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     *
     * @param bclbiome {@link BCLBiome}
     * @return {@link BCLBiome}
     */
    static BCLBiome registerBiome(BCLBiome bclbiome, Registry<Biome> registryOrNull) {
        BiomeType dim = bclbiome.getIntendedType();
        if (registryOrNull != null
                && bclbiome.biomeToRegister != null
                && registryOrNull.get(bclbiome.getID()) == null) {
            Registry.register(registryOrNull, bclbiome.getBiomeKey(), bclbiome.biomeToRegister);

            BCLBiomeRegistry.register(bclbiome);
        }

        if (dim != null && dim.is(BiomeType.NETHER)) {
            TagManager.BIOMES.add(BiomeTags.IS_NETHER, bclbiome.getBiomeKey());
            TagManager.BIOMES.add(CommonBiomeTags.IN_NETHER, bclbiome.getBiomeKey());
        } else if (dim != null && dim.is(BiomeType.END)) {
            TagManager.BIOMES.add(CommonBiomeTags.IN_END, bclbiome.getBiomeKey());
            TagManager.BIOMES.add(CommonBiomeTags.IN_END, bclbiome.getBiomeKey());
        }

        bclbiome.afterRegistration();

        return bclbiome;
    }

    public static BCLBiome registerSubBiome(BCLBiome parent, BCLBiome subBiome) {
        return registerSubBiome(
                parent,
                subBiome,
                parent.getIntendedType()
        );
    }

    public static BCLBiome registerSubBiome(BCLBiome parent, Biome subBiome, float genChance) {
        return registerSubBiome(
                parent,
                subBiome,
                genChance,
                parent.getIntendedType()
        );
    }

    public static BCLBiome registerSubBiome(BCLBiome parent, BCLBiome subBiome, BiomeType dim) {
        registerBiome(subBiome, dim);
        parent.addSubBiome(subBiome);

        return subBiome;
    }

    public static BCLBiome registerSubBiome(BCLBiome parent, Biome biome, float genChance, BiomeType dim) {
        BCLBiome subBiome = new BCLBiome(biome, VanillaBiomeSettings.createVanilla().setGenChance(genChance).build());
        return registerSubBiome(parent, subBiome, dim);
    }

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
     *
     * @param biome {@link BCLBiome}
     * @return {@link BCLBiome}
     */
    public static BCLBiome registerEndLandBiome(BCLBiome biome) {
        registerBiome(biome, BiomeType.BCL_END_LAND);

        float weight = biome.getGenChance();
        ResourceKey<Biome> key = biome.getBiomeKey();

        if (biome.isEdgeBiome()) {
            ResourceKey<Biome> parentKey = biome.getParentBiome().getBiomeKey();
            TheEndBiomes.addMidlandsBiome(parentKey, key, weight);
        } else {
            TheEndBiomes.addHighlandsBiome(key, weight);
        }

        return biome;
    }


    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a void biome (will generate only in the End void - between islands).
     *
     * @param biome {@link BCLBiome}
     * @return {@link BCLBiome}
     */
    public static BCLBiome registerEndVoidBiome(BCLBiome biome) {
        registerBiome(biome, BiomeType.BCL_END_VOID);

        float weight = biome.getGenChance();
        ResourceKey<Biome> key = biome.getBiomeKey();
        if (!biome.isEdgeBiome()) {
            TheEndBiomes.addSmallIslandsBiome(key, weight);
        }
        return biome;
    }

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a center island
     * biome (will generate only on the center island).
     *
     * @param biome {@link BCLBiome}
     * @return {@link BCLBiome}
     */
    public static BCLBiome registerEndCenterBiome(BCLBiome biome) {
        registerBiome(biome, BiomeType.BCL_END_CENTER);

        float weight = biome.getGenChance();
        ResourceKey<Biome> key = biome.getBiomeKey();
        if (!biome.isEdgeBiome()) {
            TheEndBiomes.addMainIslandBiome(key, weight);
        }
        return biome;
    }

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a barrens island
     * biome (will generate on the edge of midland biomes on the larger islands).
     *
     * @param biome {@link BCLBiome}
     * @return {@link BCLBiome}
     */
    public static BCLBiome registerEndBarrensBiome(BCLBiome highlandBiome, BCLBiome biome) {
        registerBiome(biome, BiomeType.BCL_END_BARRENS);

        float weight = biome.getGenChance();
        ResourceKey<Biome> key = biome.getBiomeKey();
        if (!biome.isEdgeBiome()) {
            ResourceKey<Biome> parentKey = highlandBiome.getBiomeKey();
            TheEndBiomes.addBarrensBiome(parentKey, key, weight);
        }
        return biome;
    }


    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     * After that biome will be added to BCLib Nether Biome Generator and into Fabric Biome API.
     *
     * @param bclBiome {@link BCLBiome}
     * @return {@link BCLBiome}
     */
    public static BCLBiome registerNetherBiome(BCLBiome bclBiome) {
        registerBiome(bclBiome, BiomeType.BCL_NETHER);

        ResourceKey<Biome> key = bclBiome.getBiomeKey();
        if (!bclBiome.isEdgeBiome()) {
            bclBiome.forEachClimateParameter(p -> NetherBiomes.addNetherBiome(key, p));
        }
        return bclBiome;
    }


    /**
     * Get {@link BCLBiome} from {@link Biome} instance on server. Used to convert world biomes to BCLBiomes.
     *
     * @param biome - {@link Holder<Biome>} from world.
     * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
     */
    @Deprecated(forRemoval = true)
    public static BCLBiome getFromBiome(Holder<Biome> biome) {
        if (InternalBiomeAPI.biomeRegistry == null) {
            return BCLBiomeRegistry.EMPTY_BIOME;
        }
        return BCLBiomeRegistry
                .getOrElseEmpty(
                        InternalBiomeAPI.registryAccess,
                        biome.unwrapKey().orElseThrow().location()
                );
    }

    /**
     * Get {@link BCLBiome} from biome on client. Used in fog rendering.
     *
     * @param biome - {@link Biome} from client world.
     * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
     */
    public static BCLBiome getRenderBiome(Biome biome) {
        BCLBiome endBiome = InternalBiomeAPI.CLIENT.get(biome);
        if (endBiome == null) {
            ResourceLocation id = WorldBootstrap.getLastRegistryAccessOrElseBuiltin()
                                                .registryOrThrow(Registry.BIOME_REGISTRY)
                                                .getKey(biome);
            endBiome = id == null
                    ? BCLBiomeRegistry.EMPTY_BIOME
                    : BCLBiomeRegistry.getOrElseEmpty(id);
            InternalBiomeAPI.CLIENT.put(biome, endBiome);
        }
        return endBiome;
    }

    /**
     * Get biome {@link ResourceKey} from given {@link Biome}.
     *
     * @param biome - {@link Biome} from server world.
     * @return biome {@link ResourceKey} or {@code null}.
     */
    @Nullable
    public static ResourceKey getBiomeKey(Biome biome) {
        if (InternalBiomeAPI.biomeRegistry != null) {
            Optional<ResourceKey<Biome>> key = InternalBiomeAPI.biomeRegistry.getResourceKey(biome);
            if (key.isPresent()) return key.get();
        }
        return BuiltinRegistries.BIOME
                .getResourceKey(biome)
                .orElseGet(null);
    }

    /**
     * Get biome {@link ResourceLocation} from given {@link Biome}.
     *
     * @param biome - {@link Biome} from server world.
     * @return biome {@link ResourceLocation}.
     */
    public static ResourceLocation getBiomeID(Biome biome) {
        ResourceLocation id = null;
        if (InternalBiomeAPI.biomeRegistry != null) {
            id = InternalBiomeAPI.biomeRegistry.getKey(biome);
        }
        if (id == null) {
            id = BuiltinRegistries.BIOME.getKey(biome);
        }

        if (id == null) {
            BCLib.LOGGER.error("Unable to get ID for " + biome + ". Falling back to empty Biome...");
            id = BCLBiomeRegistry.EMPTY_BIOME.getID();
        }

        return id;
    }

    /**
     * Get biome {@link ResourceLocation} from given {@link Biome}.
     *
     * @param biome - {@link Holder<Biome>} from server world.
     * @return biome {@link ResourceLocation}.
     */
    public static ResourceLocation getBiomeID(Holder<Biome> biome) {
        return biome
                .unwrapKey()
                .map(h -> h.location())
                .orElse(null);
    }

    public static ResourceKey getBiomeKey(Holder<Biome> biome) {
        return biome.unwrapKey().orElse(null);
    }

    public static ResourceKey getBiomeKeyOrThrow(Holder<Biome> biome) {
        return biome.unwrapKey().orElseThrow();
    }

    public static Holder<Biome> getBiomeHolder(BCLBiome biome) {
        return getBiomeHolder(biome.getBiomeKey());
    }

    public static Holder<Biome> getBiomeHolder(Biome biome) {
        Optional<ResourceKey<Biome>> key = Optional.empty();
        if (InternalBiomeAPI.biomeRegistry != null) {
            key = InternalBiomeAPI.biomeRegistry.getResourceKey(biome);
        } else {
            key = BuiltinRegistries.BIOME.getResourceKey(biome);
        }

        return getBiomeHolder(key.orElseThrow());
    }

    public static Holder<Biome> getBiomeHolder(ResourceKey<Biome> biomeKey) {
        if (InternalBiomeAPI.biomeRegistry != null) {
            return InternalBiomeAPI.biomeRegistry.getOrCreateHolderOrThrow(biomeKey);
        }
        return BuiltinRegistries.BIOME.getOrCreateHolderOrThrow(biomeKey);
    }

    public static Holder<Biome> getBiomeHolder(ResourceLocation biome) {
        return getBiomeHolder(ResourceKey.create(Registry.BIOME_REGISTRY, biome));
    }

    /**
     * Get {@link BCLBiome} from given {@link ResourceLocation}.
     *
     * @param biomeID - biome {@link ResourceLocation}.
     * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
     */
    public static BCLBiome getBiome(ResourceLocation biomeID) {
        if (biomeID == null) return BCLBiomeRegistry.EMPTY_BIOME;
        return BCLBiomeRegistry.getOrElseEmpty(biomeID);
    }

    /**
     * Get {@link BCLBiome} from given {@link Biome}.
     *
     * @param biome - biome {@link Biome}.
     * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
     */
    public static BCLBiome getBiome(Biome biome) {
        return getBiome(BiomeAPI.getBiomeID(biome));
    }

    /**
     * Get {@link BCLBiome} from given {@link Biome}.
     *
     * @param biome - biome {@link Biome}.
     * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
     */
    public static BCLBiome getBiome(Holder<Biome> biome) {
        return getBiome(BiomeAPI.getBiomeID(biome));
    }

    /**
     * Check if biome with {@link ResourceLocation} exists in API registry.
     *
     * @param biomeID - biome {@link ResourceLocation}.
     * @return {@code true} if biome exists in API registry and {@code false} if not.
     */
    public static boolean hasBiome(ResourceLocation biomeID) {
        return BCLBiomeRegistry.get(biomeID) != null;
    }

    public static Holder<Biome> getFromRegistry(ResourceLocation biomeID) {
        if (InternalBiomeAPI.biomeRegistry != null)
            return InternalBiomeAPI.biomeRegistry.getHolder(ResourceKey.create(Registry.BIOME_REGISTRY, biomeID))
                                                 .orElseThrow();
        return getFromBuiltinRegistry(biomeID);
    }

    @Nullable
    public static Holder<Biome> getFromRegistry(ResourceKey<Biome> key) {
        if (InternalBiomeAPI.biomeRegistry != null)
            return InternalBiomeAPI.biomeRegistry.getHolder(key).orElseThrow();
        return getFromBuiltinRegistry(key);
    }

    @Nullable
    public static Holder<Biome> getFromBuiltinRegistry(ResourceLocation biomeID) {
        return BuiltinRegistries.BIOME.getHolder(ResourceKey.create(Registry.BIOME_REGISTRY, biomeID)).orElse(null);
    }

    @Nullable
    public static Holder<Biome> getFromBuiltinRegistry(ResourceKey<Biome> key) {
        return BuiltinRegistries.BIOME.getHolder(key).orElse(null);
    }

    @Deprecated(forRemoval = true)
    public static boolean registryContains(ResourceKey<Biome> key) {
        if (InternalBiomeAPI.biomeRegistry != null)
            return InternalBiomeAPI.biomeRegistry.containsKey(key);
        return builtinRegistryContains(key);
    }

    @Nullable
    @Deprecated(forRemoval = true)
    public static boolean builtinRegistryContains(ResourceKey<Biome> key) {
        return BuiltinRegistries.BIOME.containsKey(key);
    }

    public static boolean isDatapackBiome(ResourceLocation biomeID) {
        return getFromBuiltinRegistry(biomeID) == null;
    }

    public static boolean wasRegisteredAs(ResourceLocation biomeID, BiomeType dim) {
        if (BCLBiomeRegistry.EMPTY_BIOME.getID().equals(biomeID))
            return false;
        return BCLBiomeRegistry.getOrElseEmpty(biomeID).getIntendedType().is(dim);
    }

    public static boolean wasRegisteredAsNetherBiome(ResourceLocation biomeID) {
        return wasRegisteredAs(biomeID, BiomeType.NETHER);
    }

    public static boolean wasRegisteredAsEndBiome(ResourceLocation biomeID) {
        return wasRegisteredAs(biomeID, BiomeType.END);
    }

    public static boolean wasRegisteredAsEndLandBiome(ResourceLocation biomeID) {
        return wasRegisteredAs(biomeID, BiomeType.END_LAND);
    }

    public static boolean wasRegisteredAsEndVoidBiome(ResourceLocation biomeID) {
        return wasRegisteredAs(biomeID, BiomeType.END_VOID);
    }

    public static boolean wasRegisteredAsEndCenterBiome(ResourceLocation biomeID) {
        return wasRegisteredAs(biomeID, BiomeType.END_CENTER);
    }

    public static boolean wasRegisteredAsEndBarrensBiome(ResourceLocation biomeID) {
        return wasRegisteredAs(biomeID, BiomeType.END_BARRENS);
    }

    /**
     * Registers new biome modification for specified dimension. Will work both for mod and datapack biomes.
     *
     * @param dimensionID  {@link ResourceLocation} dimension ID, example: Level.OVERWORLD or "minecraft:overworld".
     * @param modification {@link BiConsumer} with {@link ResourceKey} biome ID and {@link Biome} parameters.
     */
    public static void registerBiomeModification(
            ResourceKey<LevelStem> dimensionID,
            BiConsumer<ResourceLocation, Holder<Biome>> modification
    ) {
        List<BiConsumer<ResourceLocation, Holder<Biome>>> modifications = InternalBiomeAPI.MODIFICATIONS.computeIfAbsent(
                dimensionID,
                k -> Lists.newArrayList()
        );
        modifications.add(modification);
    }

    /**
     * Registers new biome modification for the Overworld. Will work both for mod and datapack biomes.
     *
     * @param modification {@link BiConsumer} with {@link ResourceLocation} biome ID and {@link Biome} parameters.
     */
    public static void registerOverworldBiomeModification(BiConsumer<ResourceLocation, Holder<Biome>> modification) {
        registerBiomeModification(LevelStem.OVERWORLD, modification);
    }

    /**
     * Registers new biome modification for the Nether. Will work both for mod and datapack biomes.
     *
     * @param modification {@link BiConsumer} with {@link ResourceLocation} biome ID and {@link Biome} parameters.
     */
    public static void registerNetherBiomeModification(BiConsumer<ResourceLocation, Holder<Biome>> modification) {
        registerBiomeModification(LevelStem.NETHER, modification);
    }

    /**
     * Registers new biome modification for the End. Will work both for mod and datapack biomes.
     *
     * @param modification {@link BiConsumer} with {@link ResourceLocation} biome ID and {@link Biome} parameters.
     */
    public static void registerEndBiomeModification(BiConsumer<ResourceLocation, Holder<Biome>> modification) {
        registerBiomeModification(LevelStem.END, modification);
    }

    /**
     * Registers new biome modification for specified dimension that is executed when all
     * BiomeTags are finalized by the game during level load. Will work both for mod and
     * datapack biomes.
     *
     * @param dimensionID  {@link ResourceLocation} dimension ID, example: Level.OVERWORLD or "minecraft:overworld".
     * @param modification {@link BiConsumer} with {@link ResourceKey} biome ID and {@link Biome} parameters.
     */
    public static void onFinishingBiomeTags(
            ResourceKey dimensionID,
            BiConsumer<ResourceLocation, Holder<Biome>> modification
    ) {
        List<BiConsumer<ResourceLocation, Holder<Biome>>> modifications = InternalBiomeAPI.TAG_ADDERS.computeIfAbsent(
                dimensionID,
                k -> Lists.newArrayList()
        );
        modifications.add(modification);
    }

    /**
     * Registers new biome modification for the Nether dimension that is executed when all
     * BiomeTags are finalized by the game during level load. Will work both for mod and
     * datapack biomes.
     *
     * @param modification {@link BiConsumer} with {@link ResourceLocation} biome ID and {@link Biome} parameters.
     */
    public static void onFinishingNetherBiomeTags(BiConsumer<ResourceLocation, Holder<Biome>> modification) {
        onFinishingBiomeTags(Level.NETHER, modification);
    }

    /**
     * Registers new biome modification for the End that is executed when all
     * BiomeTags are finalized by the game during level load. Will work both for mod and
     * datapack biomes.
     *
     * @param modification {@link BiConsumer} with {@link ResourceLocation} biome ID and {@link Biome} parameters.
     */
    public static void onFinishingEndBiomeTags(BiConsumer<ResourceLocation, Holder<Biome>> modification) {
        onFinishingBiomeTags(Level.END, modification);
    }


    /**
     * Create a unique sort order for all Features of the Biome. This method is automatically called for each Biome
     * after all biome Modifications were executed.
     *
     * @param biome The {@link Biome} to sort the features for
     */
    public static void sortBiomeFeatures(Holder<Biome> biome) {
        sortBiomeFeatures(biome.value());
    }

    static void sortBiomeFeatures(Biome biome) {
//        BiomeGenerationSettings settings = biome.getGenerationSettings();
//        BiomeGenerationSettingsAccessor accessor = (BiomeGenerationSettingsAccessor) settings;
//        List<HolderSet<PlacedFeature>> featureList = CollectionsUtil.getMutable(accessor.bclib_getFeatures());
//        final int size = featureList.size();
//        for (int i = 0; i < size; i++) {
//            List<Holder<PlacedFeature>> features = getFeaturesListCopy(featureList, i);
//            sortFeatures(features);
//            featureList.set(i, HolderSet.direct(features));
//        }
//        accessor.bclib_setFeatures(featureList);
    }

    /**
     * Adds new features to existing biome.
     *
     * @param biome   {@link Biome} to add features in.
     * @param feature {@link ConfiguredFeature} to add.
     */
    public static void addBiomeFeature(Holder<Biome> biome, BCLFeature feature) {
        addBiomeFeature(biome, feature.getDecoration(), feature.getPlacedFeature());
    }

    /**
     * Adds new features to existing biome.
     *
     * @param biome   {@link Biome} to add features in.
     * @param feature {@link ConfiguredFeature} to add.
     */
    public static void addBiomeFeature(
            Holder<Biome> biome,
            org.betterx.bclib.api.v3.levelgen.features.BCLFeature feature
    ) {
        addBiomeFeature(biome, feature.getDecoration(), feature.getPlacedFeature());
    }

    /**
     * Adds new features to existing biome.
     *
     * @param biome       {@link Biome} to add features in.
     * @param step        a {@link Decoration} step for the feature.
     * @param featureList {@link ConfiguredFeature} to add.
     */
    public static void addBiomeFeature(Holder<Biome> biome, Decoration step, Holder<PlacedFeature>... featureList) {
        addBiomeFeature(biome, step, List.of(featureList));
    }

    /**
     * Adds new features to existing biome.
     *
     * @param biome              {@link Biome} to add features in.
     * @param step               a {@link Decoration} step for the feature.
     * @param additionalFeatures List of {@link ConfiguredFeature} to add.
     */
    private static void addBiomeFeature(
            Holder<Biome> biome,
            Decoration step,
            List<Holder<PlacedFeature>> additionalFeatures
    ) {
        BiomeGenerationSettingsAccessor accessor = (BiomeGenerationSettingsAccessor) biome.value()
                                                                                          .getGenerationSettings();
        List<HolderSet<PlacedFeature>> allFeatures = CollectionsUtil.getMutable(accessor.bclib_getFeatures());
        List<Holder<PlacedFeature>> features = getFeaturesListCopy(allFeatures, step);

        for (var feature : additionalFeatures) {
            if (!features.contains(feature))
                features.add(feature);
        }

        allFeatures.set(step.ordinal(), HolderSet.direct(features));
        final Supplier<List<ConfiguredFeature<?, ?>>> flowerFeatures = Suppliers.memoize(() -> allFeatures.stream()
                                                                                                          .flatMap(
                                                                                                                  HolderSet::stream)
                                                                                                          .map(Holder::value)
                                                                                                          .flatMap(
                                                                                                                  PlacedFeature::getFeatures)
                                                                                                          .filter(configuredFeature -> configuredFeature.feature() == Feature.FLOWER)
                                                                                                          .collect(
                                                                                                                  ImmutableList.toImmutableList()));
        final Supplier<Set<PlacedFeature>> featureSet = Suppliers.memoize(() -> allFeatures.stream()
                                                                                           .flatMap(HolderSet::stream)
                                                                                           .map(Holder::value)
                                                                                           .collect(Collectors.toSet()));

        accessor.bclib_setFeatures(allFeatures);
        accessor.bclib_setFeatureSet(featureSet);
        accessor.bclib_setFlowerFeatures(flowerFeatures);
    }


    /**
     * Adds mob spawning to specified biome.
     *
     * @param biome         {@link Biome} to add mob spawning.
     * @param entityType    {@link EntityType} mob type.
     * @param weight        spawn weight.
     * @param minGroupCount minimum mobs in group.
     * @param maxGroupCount maximum mobs in group.
     */
    public static <M extends Mob> void addBiomeMobSpawn(
            Holder<Biome> biome,
            EntityType<M> entityType,
            int weight,
            int minGroupCount,
            int maxGroupCount
    ) {
        final MobCategory category = entityType.getCategory();
        MobSpawnSettingsAccessor accessor = (MobSpawnSettingsAccessor) biome.value().getMobSettings();
        Map<MobCategory, WeightedRandomList<SpawnerData>> spawners = CollectionsUtil.getMutable(accessor.bcl_getSpawners());
        List<SpawnerData> mobs = spawners.containsKey(category)
                ? CollectionsUtil.getMutable(spawners.get(category)
                                                     .unwrap())
                : Lists.newArrayList();
        mobs.add(new SpawnerData(entityType, weight, minGroupCount, maxGroupCount));
        spawners.put(category, WeightedRandomList.create(mobs));
        accessor.bcl_setSpawners(spawners);
    }


    public static Optional<BlockState> findTopMaterial(WorldGenLevel world, BlockPos pos) {
        return findTopMaterial(getBiome(world.getBiome(pos)));
    }

    public static Optional<BlockState> findTopMaterial(Holder<Biome> biome) {
        return findTopMaterial(getBiome(biome.value()));
    }

    public static Optional<BlockState> findTopMaterial(Biome biome) {
        return findTopMaterial(getBiome(biome));
    }

    public static Optional<BlockState> findTopMaterial(BCLBiome biome) {
        if (biome instanceof SurfaceMaterialProvider smp) {
            return Optional.of(smp.getTopMaterial());
        }
        return Optional.empty();
    }

    public static Optional<BlockState> findUnderMaterial(Holder<Biome> biome) {
        return findUnderMaterial(getBiome(biome.value()));
    }

    public static Optional<BlockState> findUnderMaterial(BCLBiome biome) {
        if (biome instanceof SurfaceMaterialProvider smp) {
            return Optional.of(smp.getUnderMaterial());
        }
        return Optional.empty();
    }

    /**
     * Set biome in chunk at specified position.
     *
     * @param chunk {@link ChunkAccess} chunk to set biome in.
     * @param pos   {@link BlockPos} biome position.
     * @param biome {@link Holder<Biome>} instance. Should be biome from world.
     */
    public static void setBiome(ChunkAccess chunk, BlockPos pos, Holder<Biome> biome) {
        int sectionY = (pos.getY() - chunk.getMinBuildHeight()) >> 4;
        PalettedContainer<Holder<Biome>> palette = chunk.getSection(sectionY).getBiomes();
        palette.set((pos.getX() & 15) >> 2, (pos.getY() & 15) >> 2, (pos.getZ() & 15) >> 2, biome);
    }

    /**
     * Set biome in world at specified position.
     *
     * @param level {@link LevelAccessor} world to set biome in.
     * @param pos   {@link BlockPos} biome position.
     * @param biome {@link Holder<Biome>} instance. Should be biome from world.
     */
    public static void setBiome(LevelAccessor level, BlockPos pos, Holder<Biome> biome) {
        ChunkAccess chunk = level.getChunk(pos);
        setBiome(chunk, pos, biome);
    }

    private static void sortFeatures(List<Holder<PlacedFeature>> features) {
//        InternalBiomeAPI.initFeatureOrder();
//
//        Set<Holder<PlacedFeature>> featuresWithoutDuplicates = Sets.newHashSet();
//        features.forEach(holder -> featuresWithoutDuplicates.add(holder));
//
//        if (featuresWithoutDuplicates.size() != features.size()) {
//            features.clear();
//            featuresWithoutDuplicates.forEach(feature -> features.add(feature));
//        }
//
//        features.forEach(feature -> {
//            InternalBiomeAPI.FEATURE_ORDER.computeIfAbsent(
//                    feature,
//                    f -> InternalBiomeAPI.FEATURE_ORDER_ID.getAndIncrement()
//            );
//        });
//
//        features.sort((f1, f2) -> {
//            int v1 = InternalBiomeAPI.FEATURE_ORDER.getOrDefault(f1, 70000);
//            int v2 = InternalBiomeAPI.FEATURE_ORDER.getOrDefault(f2, 70000);
//            return Integer.compare(v1, v2);
//        });
    }


    private static List<Holder<PlacedFeature>> getFeaturesListCopy(
            List<HolderSet<PlacedFeature>> features,
            Decoration step
    ) {
        return getFeaturesListCopy(features, step.ordinal());
    }

    private static List<Holder<PlacedFeature>> getFeaturesListCopy(List<HolderSet<PlacedFeature>> features, int index) {
        while (features.size() <= index) {
            features.add(HolderSet.direct(Lists.newArrayList()));
        }
        return features.get(index).stream().collect(Collectors.toList());
    }


    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     * After that biome will be added to BCLib Nether Biome Generator and into Fabric Biome API.
     *
     * @param biome {@link BCLBiome}
     * @return {@link BCLBiome}
     */
    @Deprecated(forRemoval = true)
    public static BCLBiome registerNetherBiome(Biome biome) {
        return InternalBiomeAPI.wrapNativeBiome(biome, -1, InternalBiomeAPI.OTHER_NETHER);
    }

    @Deprecated(forRemoval = true)
    public static BCLBiome registerEndLandBiome(Holder<Biome> biome) {
        return InternalBiomeAPI.wrapNativeBiome(biome.unwrapKey().orElseThrow(), InternalBiomeAPI.OTHER_END_LAND);
    }

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a void biome (will generate only in the End void - between islands).
     *
     * @param biome {@link BCLBiome}
     * @return {@link BCLBiome}
     */
    @Deprecated(forRemoval = true)
    public static BCLBiome registerEndVoidBiome(Holder<Biome> biome) {
        return InternalBiomeAPI.wrapNativeBiome(biome.unwrapKey().orElseThrow(), InternalBiomeAPI.OTHER_END_VOID);
    }

    /**
     * Register {@link BCLBiome} wrapper for {@link Biome}.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
     *
     * @param biome     {@link BCLBiome};
     * @param genChance float generation chance.
     * @return {@link BCLBiome}
     */
    @Deprecated(forRemoval = true)
    public static BCLBiome registerEndLandBiome(Holder<Biome> biome, float genChance) {
        return InternalBiomeAPI.wrapNativeBiome(
                biome.unwrapKey().orElseThrow(),
                genChance,
                InternalBiomeAPI.OTHER_END_LAND
        );
    }

    /**
     * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
     * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a void biome (will generate only in the End void - between islands).
     *
     * @param biome     {@link BCLBiome}.
     * @param genChance float generation chance.
     * @return {@link BCLBiome}
     */
    @Deprecated(forRemoval = true)
    public static BCLBiome registerEndVoidBiome(Holder<Biome> biome, float genChance) {
        return InternalBiomeAPI.wrapNativeBiome(
                biome.unwrapKey().orElseThrow(),
                genChance,
                InternalBiomeAPI.OTHER_END_VOID
        );
    }


    @Deprecated(forRemoval = true)
    public static BCLBiome registerEndBiome(Holder<Biome> biome) {
        BCLBiome bclBiome = new BCLBiome(biome.value(), null);

        registerBiome(bclBiome, BiomeType.END);
        return bclBiome;
    }


    @Deprecated(forRemoval = true)
    public static BCLBiome registerCenterBiome(Holder<Biome> biome) {
        BCLBiome bclBiome = new BCLBiome(biome.value(), null);

        registerBiome(bclBiome, BiomeType.END_CENTER);
        return bclBiome;
    }
}
