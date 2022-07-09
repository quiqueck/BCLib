package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.bclib.util.WeightedList;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;


public class BCLBiome extends BCLBiomeSettings implements BiomeData {
    public static final Codec<BCLBiome> CODEC = RecordCodecBuilder.create(instance -> codecWithSettings(instance).apply(
            instance,
            BCLBiome::new
    ));
    public static final KeyDispatchDataCodec<BCLBiome> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    public KeyDispatchDataCodec<? extends BCLBiome> codec() {
        return KEY_CODEC;
    }

    private static class CodecAttributes<T extends BCLBiome> {
        public RecordCodecBuilder<T, Float> t0 = Codec.FLOAT.fieldOf("terrainHeight")
                                                            .orElse(0.1f)
                                                            .forGetter((T o1) -> o1.terrainHeight);

        public RecordCodecBuilder<T, Float> t1 = Codec.FLOAT.fieldOf("fogDensity")
                                                            .orElse(1.0f)
                                                            .forGetter((T o1) -> o1.fogDensity);
        public RecordCodecBuilder<T, Float> t2 = Codec.FLOAT.fieldOf("genChance")
                                                            .orElse(1.0f)
                                                            .forGetter((T o1) -> o1.genChance);
        public RecordCodecBuilder<T, Integer> t3 = Codec.INT.fieldOf("edgeSize")
                                                            .orElse(0)
                                                            .forGetter((T o1) -> o1.edgeSize);
        public RecordCodecBuilder<T, Boolean> t4 = Codec.BOOL.fieldOf("vertical")
                                                             .orElse(false)
                                                             .forGetter((T o1) -> o1.vertical);
        public RecordCodecBuilder<T, Optional<ResourceLocation>> t5 =
                ResourceLocation.CODEC
                        .optionalFieldOf("edge")
                        .orElse(Optional.empty())
                        .forGetter((T o1) -> o1.edge == null
                                ? Optional.empty()
                                : Optional.of(o1.edge.biomeID));
        public RecordCodecBuilder<T, ResourceLocation> t6 =
                ResourceLocation.CODEC.fieldOf("biome")
                                      .forGetter((T o) -> ((BCLBiome) o).biomeID);
        public RecordCodecBuilder<T, Optional<List<Climate.ParameterPoint>>> t7 =
                Climate.ParameterPoint.CODEC.listOf()
                                            .optionalFieldOf("parameter_points")
                                            .orElse(Optional.of(List.of()))
                                            .forGetter((T o) ->
                                                    o.parameterPoints == null || o.parameterPoints.isEmpty()
                                                            ? Optional.empty()
                                                            : Optional.of(o.parameterPoints));

        public RecordCodecBuilder<T, Optional<ResourceLocation>> t8 =
                ResourceLocation.CODEC.optionalFieldOf("parent")
                                      .orElse(Optional.empty())
                                      .forGetter(
                                              (T o1) ->
                                                      ((BCLBiome) o1).biomeParent == null
                                                              ? Optional.empty()
                                                              : Optional.of(
                                                                      ((BCLBiome) o1).biomeParent.biomeID));
        public RecordCodecBuilder<T, Optional<WeightedList<ResourceLocation>>> t9 =
                WeightedList.listCodec(
                                    ResourceLocation.CODEC,
                                    "biomes",
                                    "biome"
                            )
                            .optionalFieldOf("sub_biomes")
                            .forGetter(
                                    (T o) -> {
                                        if (o.subbiomes == null
                                                || o.subbiomes.isEmpty()
                                                || (o.subbiomes.size() == 1 && o.subbiomes.contains(
                                                o))) {
                                            return Optional.empty();
                                        }
                                        return Optional.of(
                                                o.subbiomes.map(
                                                        b -> b.biomeID));
                                    });
        public RecordCodecBuilder<T, Optional<String>> t10 =
                Codec.STRING.optionalFieldOf("intended_for")
                            .orElse(Optional.of(BiomeAPI.BiomeType.NONE.getName()))
                            .forGetter((T o) ->
                                    ((BCLBiome) o).intendedType == null
                                            ? Optional.empty()
                                            : Optional.of(((BCLBiome) o).intendedType.getName()));
    }

    public static <T extends BCLBiome, P12> Products.P12<RecordCodecBuilder.Mu<T>, Float, Float, Float, Integer, Boolean, Optional<ResourceLocation>, ResourceLocation, Optional<List<Climate.ParameterPoint>>, Optional<ResourceLocation>, Optional<WeightedList<ResourceLocation>>, Optional<String>, P12> codecWithSettings(
            RecordCodecBuilder.Instance<T> instance,
            final RecordCodecBuilder<T, P12> p12
    ) {
        CodecAttributes<T> a = new CodecAttributes<>();
        return instance.group(a.t0, a.t1, a.t2, a.t3, a.t4, a.t5, a.t6, a.t7, a.t8, a.t9, a.t10, p12);
    }

    public static <T extends BCLBiome, P12, P13> Products.P13<RecordCodecBuilder.Mu<T>, Float, Float, Float, Integer, Boolean, Optional<ResourceLocation>, ResourceLocation, Optional<List<Climate.ParameterPoint>>, Optional<ResourceLocation>, Optional<WeightedList<ResourceLocation>>, Optional<String>, P12, P13> codecWithSettings(
            RecordCodecBuilder.Instance<T> instance,
            final RecordCodecBuilder<T, P12> p12,
            final RecordCodecBuilder<T, P13> p13
    ) {
        CodecAttributes<T> a = new CodecAttributes<>();
        return instance.group(a.t0, a.t1, a.t2, a.t3, a.t4, a.t5, a.t6, a.t7, a.t8, a.t9, a.t10, p12, p13);
    }

    public static <T extends BCLBiome, P12, P13, P14, P15> Products.P15<RecordCodecBuilder.Mu<T>, Float, Float, Float, Integer, Boolean, Optional<ResourceLocation>, ResourceLocation, Optional<List<Climate.ParameterPoint>>, Optional<ResourceLocation>, Optional<WeightedList<ResourceLocation>>, Optional<String>, P12, P13, P14, P15> codecWithSettings(
            RecordCodecBuilder.Instance<T> instance,
            final RecordCodecBuilder<T, P12> p12,
            final RecordCodecBuilder<T, P13> p13,
            final RecordCodecBuilder<T, P14> p14,
            final RecordCodecBuilder<T, P15> p15
    ) {
        CodecAttributes<T> a = new CodecAttributes<>();
        return instance.group(a.t0, a.t1, a.t2, a.t3, a.t4, a.t5, a.t6, a.t7, a.t8, a.t9, a.t10, p12, p13, p14, p15);
    }

    public static <T extends BCLBiome, P12, P13, P14> Products.P14<RecordCodecBuilder.Mu<T>, Float, Float, Float, Integer, Boolean, Optional<ResourceLocation>, ResourceLocation, Optional<List<Climate.ParameterPoint>>, Optional<ResourceLocation>, Optional<WeightedList<ResourceLocation>>, Optional<String>, P12, P13, P14> codecWithSettings(
            RecordCodecBuilder.Instance<T> instance,
            final RecordCodecBuilder<T, P12> p12,
            final RecordCodecBuilder<T, P13> p13,
            final RecordCodecBuilder<T, P14> p14
    ) {
        CodecAttributes<T> a = new CodecAttributes<>();
        return instance.group(a.t0, a.t1, a.t2, a.t3, a.t4, a.t5, a.t6, a.t7, a.t8, a.t9, a.t10, p12, p13, p14);
    }

    public static <T extends BCLBiome> Products.P11<RecordCodecBuilder.Mu<T>, Float, Float, Float, Integer, Boolean, Optional<ResourceLocation>, ResourceLocation, Optional<List<Climate.ParameterPoint>>, Optional<ResourceLocation>, Optional<WeightedList<ResourceLocation>>, Optional<String>> codecWithSettings(
            RecordCodecBuilder.Instance<T> instance
    ) {
        CodecAttributes<T> a = new CodecAttributes<>();
        return instance.group(a.t0, a.t1, a.t2, a.t3, a.t4, a.t5, a.t6, a.t7, a.t8, a.t9, a.t10);
    }

    protected final WeightedList<BCLBiome> subbiomes = new WeightedList<>();
    private final Map<String, Object> customData = Maps.newHashMap();
    private final ResourceLocation biomeID;
    private final ResourceKey<Biome> biomeKey;
    final Biome biomeToRegister;

    protected final List<Climate.ParameterPoint> parameterPoints = Lists.newArrayList();

    private BCLBiome biomeParent;

    private BiomeAPI.BiomeType intendedType = BiomeAPI.BiomeType.NONE;

    protected BCLBiome(
            float terrainHeight,
            float fogDensity,
            float genChance,
            int edgeSize,
            boolean vertical,
            Optional<ResourceLocation> edge,
            ResourceLocation biomeID,
            Optional<List<Climate.ParameterPoint>> parameterPoints,
            Optional<ResourceLocation> biomeParent,
            Optional<WeightedList<ResourceLocation>> subbiomes,
            Optional<String> intendedType
    ) {
        super(terrainHeight, fogDensity, genChance, edgeSize, vertical, edge.map(BiomeAPI::getBiome).orElse(null));
        biomeToRegister = null;
        this.biomeID = biomeID;
        this.biomeKey = ResourceKey.create(Registry.BIOME_REGISTRY, biomeID);
        if (subbiomes.isEmpty() || subbiomes.get().size() == 0) {
            this.subbiomes.add(this, 1);
        } else {
            this.subbiomes.addAll(subbiomes.get().map(BiomeAPI::getBiome));
        }
        this.biomeParent = biomeParent.map(BiomeAPI::getBiome).orElse(null);
        if (parameterPoints.isPresent()) this.parameterPoints.addAll(parameterPoints.get());
        this.setIntendedType(intendedType.map(t -> BiomeAPI.BiomeType.create(t)).orElse(BiomeAPI.BiomeType.NONE));


    }

    /**
     * Create wrapper for existing biome using its {@link ResourceLocation} identifier.
     *
     * @param biomeKey {@link ResourceKey} for the {@link Biome}.
     */
    protected BCLBiome(ResourceKey<Biome> biomeKey) {
        this(biomeKey.location());
    }

    /**
     * Create wrapper for existing biome using its {@link ResourceLocation} identifier.
     *
     * @param biomeID {@link ResourceLocation} biome ID.
     */
    protected BCLBiome(ResourceLocation biomeID) {
        this(ResourceKey.create(Registry.BIOME_REGISTRY, biomeID), null);
    }

    /**
     * Create wrapper for existing biome using biome instance from {@link BuiltinRegistries}.
     *
     * @param biomeToRegister {@link Biome} to wrap.
     */
    @Deprecated(forRemoval = true)
    protected BCLBiome(Biome biomeToRegister) {
        this(biomeToRegister, null);
    }

    /**
     * Create wrapper for existing biome using biome instance from {@link BuiltinRegistries}.
     *
     * @param biomeToRegister {@link Biome} to wrap.
     * @param settings        The Settings for this Biome or {@code null} if you want to apply default settings
     */
    @Deprecated(forRemoval = true)
    protected BCLBiome(Biome biomeToRegister, VanillaBiomeSettings settings) {
        this(BiomeAPI.getBiomeID(biomeToRegister), biomeToRegister, settings);
    }

    /**
     * Create wrapper for existing biome using biome instance from {@link BuiltinRegistries}.
     *
     * @param biomeToRegister {@link Biome} to wrap.
     * @param biomeID         Teh ResoureLocation for this Biome
     */
    @Deprecated(forRemoval = true)
    //this constructor should become package private and not get removed
    public BCLBiome(ResourceLocation biomeID, Biome biomeToRegister) {
        this(biomeID, biomeToRegister, null);
    }

    /**
     * Create a new Biome
     *
     * @param biomeID         {@link ResourceLocation} biome ID.
     * @param biomeToRegister {@link Biome} to wrap.
     * @param defaults        The Settings for this Biome or null if you want to apply the defaults
     */
    protected BCLBiome(ResourceLocation biomeID, Biome biomeToRegister, BCLBiomeSettings defaults) {
        this(ResourceKey.create(Registry.BIOME_REGISTRY, biomeID), biomeToRegister, defaults);
    }

    /**
     * Create a new Biome
     *
     * @param biomeKey {@link ResourceKey<Biome>} of the wrapped Biome
     * @param defaults The Settings for this Biome or null if you want to apply the defaults
     */
    protected BCLBiome(ResourceKey<Biome> biomeKey, BCLBiomeSettings defaults) {
        this(biomeKey, null, defaults);
    }

    /**
     * Create a new Biome
     *
     * @param biomeKey        {@link ResourceKey<Biome>} of the wrapped Biome
     * @param biomeToRegister The biome you want to use when this instance gets registered through the {@link BiomeAPI}
     * @param defaults        The Settings for this Biome or null if you want to apply the defaults
     */
    protected BCLBiome(ResourceKey<Biome> biomeKey, Biome biomeToRegister, BCLBiomeSettings defaults) {
        this.biomeToRegister = biomeToRegister;
        this.subbiomes.add(this, 1.0F);
        this.biomeID = biomeKey.location();
        this.biomeKey = biomeKey;

        if (defaults != null) {
            defaults.applyWithDefaults(this);
        }
    }

    /**
     * Changes the intended Type for this Biome
     *
     * @param type the new type
     * @return the same instance
     */
    protected BCLBiome setIntendedType(BiomeAPI.BiomeType type) {
        return _setIntendedType(type);
    }

    BCLBiome _setIntendedType(BiomeAPI.BiomeType type) {
        this.intendedType = type;
        return this;
    }

    public BiomeAPI.BiomeType getIntendedType() {
        return this.intendedType;
    }

    /**
     * Get current biome edge.
     *
     * @return {@link BCLBiome} edge.
     */
    @Nullable
    public BCLBiome getEdge() {
        return edge;
    }

    /**
     * Set biome edge for this biome instance.
     *
     * @param edge {@link BCLBiome} as the edge biome.
     * @return same {@link BCLBiome}.
     */
    BCLBiome setEdge(BCLBiome edge) {
        this.edge = edge;
        edge.biomeParent = this;
        return this;
    }

    /**
     * Set biome edge for this biome instance. If there is already an edge, the
     * biome is added as subBiome to the current edge-biome
     *
     * @param edge The new edge
     * @return same {@link BCLBiome}.
     */
    public BCLBiome addEdge(BCLBiome edge) {
        if (this.edge != null) {
            this.edge.addSubBiome(edge);
        } else {
            this.setEdge(edge);
        }
        return this;
    }

    /**
     * Adds sub-biome into this biome instance. Biome chance will be interpreted as a sub-biome generation chance.
     * Biome itself has chance 1.0 compared to all its sub-biomes.
     *
     * @param biome {@link Random} to be added.
     * @return same {@link BCLBiome}.
     */
    public BCLBiome addSubBiome(BCLBiome biome) {
        biome.biomeParent = this;
        subbiomes.add(biome, biome.getGenChance());
        return this;
    }

    /**
     * Checks if specified biome is a sub-biome of this one.
     *
     * @param biome {@link Random}.
     * @return true if this instance contains specified biome as a sub-biome.
     */
    public boolean containsSubBiome(BCLBiome biome) {
        return subbiomes.contains(biome);
    }

    /**
     * Getter for a random sub-biome from all existing sub-biomes. Will return biome itself if there are no sub-biomes.
     *
     * @param random {@link Random}.
     * @return {@link BCLBiome}.
     */
    public BCLBiome getSubBiome(WorldgenRandom random) {
        return subbiomes.get(random);
    }

    public void forEachSubBiome(BiConsumer<BCLBiome, Float> consumer) {
        for (int i = 0; i < subbiomes.size(); i++)
            consumer.accept(subbiomes.get(i), subbiomes.getWeight(i));
    }

    /**
     * Getter for parent {@link BCLBiome} or null if there are no parent biome.
     *
     * @return {@link BCLBiome} or null.
     */
    @Nullable
    public BCLBiome getParentBiome() {
        return this.biomeParent;
    }

    /**
     * Compares biome instances (directly) and their parents. Used in custom world generator.
     *
     * @param biome {@link BCLBiome}
     * @return true if biome or its parent is same.
     */
    public boolean isSame(BCLBiome biome) {
        return biome == this || (biome.biomeParent != null && biome.biomeParent == this);
    }

    /**
     * Getter for biome identifier.
     *
     * @return {@link ResourceLocation}
     */
    public ResourceLocation getID() {
        return biomeID;
    }


    /**
     * Getter for biome from buil-in registry. For datapack biomes will be same as actual biome.
     *
     * @return {@link Biome}.
     */
    @Deprecated(forRemoval = true)
    public Biome getBiome() {
        if (biomeToRegister != null) return biomeToRegister;
        return BiomeAPI.getFromBuiltinRegistry(biomeKey).value();
    }

    /**
     * Getter for biomeKey
     *
     * @return {@link ResourceKey<Biome>}.
     */
    public ResourceKey<Biome> getBiomeKey() {
        return biomeKey;
    }

    public ResourceKey<BCLBiome> getBCLBiomeKey() {
        return ResourceKey.create(BCLBiomeRegistry.BCL_BIOMES_REGISTRY, biomeID);
    }

    /**
     * For internal use from BiomeAPI only
     */
    void afterRegistration() {

    }


    /**
     * Getter for custom data. Will get custom data object or null if object doesn't exists.
     *
     * @param name {@link String} name of data object.
     * @return object value or null.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = true)
    public <T> T getCustomData(String name) {
        return (T) customData.get(name);
    }

    /**
     * Getter for custom data. Will get custom data object or default value if object doesn't exists.
     *
     * @param name         {@link String} name of data object.
     * @param defaultValue object default value.
     * @return object value or default value.
     */
    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = true)
    public <T> T getCustomData(String name, T defaultValue) {
        return (T) customData.getOrDefault(name, defaultValue);
    }

    /**
     * Adds custom data object to this biome instance.
     *
     * @param name {@link String} name of data object.
     * @param obj  any data to add.
     * @return same {@link BCLBiome}.
     */
    @Deprecated(forRemoval = true)
    public BCLBiome addCustomData(String name, Object obj) {
        customData.put(name, obj);
        return this;
    }

    /**
     * Adds custom data object to this biome instance.
     *
     * @param data a {@link Map} with custom data.
     * @return same {@link BCLBiome}.
     */
    @Deprecated(forRemoval = true)
    public BCLBiome addCustomData(Map<String, Object> data) {
        customData.putAll(data);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        BCLBiome biome = (BCLBiome) obj;
        return biome != null && biomeID.equals(biome.biomeID);
    }

    @Override
    public int hashCode() {
        return biomeID.hashCode();
    }

    @Override
    public String toString() {
        return biomeID.toString();
    }


    /**
     * Adds structures to this biome. For internal use only.
     * Used inside {@link BCLBiomeBuilder}.
     */
    void addClimateParameters(List<Climate.ParameterPoint> params) {
        this.parameterPoints.addAll(params);
    }

    public void forEachClimateParameter(Consumer<Climate.ParameterPoint> consumer) {
        this.parameterPoints.forEach(consumer);
    }

    /**
     * Returns the group used in the config Files for this biome
     * <p>
     * Example: {@code Configs.BIOMES_CONFIG.getFloat(configGroup(), "generation_chance", 1.0);}
     *
     * @return The group name
     */
    public String configGroup() {
        return biomeID.getNamespace() + "." + biomeID.getPath();
    }

    private final boolean didLoadConfig = false;

    public boolean isEdgeBiome() {
        if (getParentBiome() == null) return false;
        return getParentBiome().edge == this;
    }

    boolean allowFabricRegistration() {
        return !isEdgeBiome();
    }
}
