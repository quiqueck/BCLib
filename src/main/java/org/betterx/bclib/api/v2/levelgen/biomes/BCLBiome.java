package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.bclib.util.WeightedList;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class BCLBiome extends BCLBiomeSettings {
    private final Set<TagKey<Biome>> biomeTags = Sets.newHashSet();
    private final WeightedList<BCLBiome> subbiomes = new WeightedList<>();
    private final Map<String, Object> customData = Maps.newHashMap();
    private final ResourceLocation biomeID;
    private final ResourceKey<Biome> biomeKey;
    final Biome biomeToRegister;

    private final List<Climate.ParameterPoint> parameterPoints = Lists.newArrayList();

    private BCLBiome biomeParent;

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
    public Biome getBiomeOld() {
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

    /**
     * For internal use from BiomeAPI only
     */
    void afterRegistration() {
        this.biomeTags.forEach(tagKey -> TagManager.BIOMES.add(tagKey, this));
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
    void addBiomeTags(Set<TagKey<Biome>> tags) {
        biomeTags.addAll(tags);
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
