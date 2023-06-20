package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.biomesource.BiomeSourceHelper;
import org.betterx.worlds.together.biomesource.MergeableBiomeSource;
import org.betterx.worlds.together.biomesource.ReloadableBiomeSource;
import org.betterx.worlds.together.world.BiomeSourceWithNoiseRelatedSettings;
import org.betterx.worlds.together.world.BiomeSourceWithSeed;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.*;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public abstract class BCLBiomeSource extends BiomeSource implements BiomeSourceWithSeed, MergeableBiomeSource<BCLBiomeSource>, BiomeSourceWithNoiseRelatedSettings, ReloadableBiomeSource {
    @FunctionalInterface
    public interface PickerAdder {
        boolean add(BCLBiome bclBiome, BiomeAPI.BiomeType type, BiomePicker picker);
    }

    @FunctionalInterface
    public interface CustomTypeFinder {
        BiomeAPI.BiomeType find(ResourceKey<Biome> biomeKey, BiomeAPI.BiomeType defaultType);
    }

    protected long currentSeed;
    protected int maxHeight;
    private boolean didCreatePickers;
    Set<Holder<Biome>> dynamicPossibleBiomes;

    protected BCLBiomeSource(long seed) {
        super();
        this.dynamicPossibleBiomes = Set.of();
        this.currentSeed = seed;
        this.didCreatePickers = false;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        reloadBiomes();
        return dynamicPossibleBiomes.stream();
    }

    @Override
    public Set<Holder<Biome>> possibleBiomes() {
        return dynamicPossibleBiomes;
    }


    protected boolean wasBound() {
        return didCreatePickers;
    }

    final public void setSeed(long seed) {
        if (seed != currentSeed) {
            BCLib.LOGGER.debug(this + "\n    --> new seed = " + seed);
            this.currentSeed = seed;
            initMap(seed);
        }
    }

    /**
     * Set world height
     *
     * @param maxHeight height of the World.
     */
    final public void setMaxHeight(int maxHeight) {
        if (this.maxHeight != maxHeight) {
            BCLib.LOGGER.debug(this + "\n    --> new height = " + maxHeight);
            this.maxHeight = maxHeight;
            onHeightChange(maxHeight);
        }
    }

    protected final void initMap(long seed) {
        BCLib.LOGGER.debug(this + "\n    --> Map Update");
        onInitMap(seed);
    }

    protected abstract void onInitMap(long newSeed);
    protected abstract void onHeightChange(int newHeight);


    @NotNull
    protected String getNamespaces() {
        return BiomeSourceHelper.getNamespaces(possibleBiomes());
    }

    protected boolean addToPicker(BCLBiome bclBiome, BiomeAPI.BiomeType type, BiomePicker picker) {
        picker.addBiome(bclBiome);
        return true;
    }

    protected BiomeAPI.BiomeType typeForUnknownBiome(ResourceKey<Biome> biomeKey, BiomeAPI.BiomeType defaultType) {
        return defaultType;
    }


    protected static Set<Holder<Biome>> populateBiomePickers(
            Map<BiomeAPI.BiomeType, BiomePicker> acceptedBiomeTypes,
            BiomeAPI.BiomeType exclusionListType,
            PickerAdder pickerAdder,
            CustomTypeFinder typeFinder
    ) {
        final RegistryAccess access = WorldBootstrap.getLastRegistryAccess();
        if (access == null) {
            if (Configs.MAIN_CONFIG.verboseLogging() && !BCLib.isDatagen()) {
                BCLib.LOGGER.info("Unable to build Biome List yet");
            }
            return null;
        }

        final Set<Holder<Biome>> allBiomes = new HashSet<>();
        final Map<BiomeAPI.BiomeType, List<String>> includeMap = Configs.BIOMES_CONFIG.getBiomeIncludeMap();
        final List<String> excludeList = Configs.BIOMES_CONFIG.getExcludeMatching(exclusionListType);
        final Registry<Biome> biomes = access.registryOrThrow(Registries.BIOME);
        final Registry<BCLBiome> bclBiomes = access.registryOrThrow(BCLBiomeRegistry.BCL_BIOMES_REGISTRY);

        final List<Map.Entry<ResourceKey<Biome>, Biome>> sortedList = biomes
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(a -> a.getKey().location().toString()))
                .toList();

        for (Map.Entry<ResourceKey<Biome>, Biome> biomeEntry : sortedList) {
            if (excludeList.contains(biomeEntry.getKey().location())) continue;

            BiomeAPI.BiomeType type = BiomeAPI.BiomeType.NONE;
            boolean foundBCLBiome = false;
            if (BCLBiomeRegistry.hasBiome(biomeEntry.getKey(), bclBiomes)) {
                foundBCLBiome = true;
                type = BCLBiomeRegistry.getBiome(biomeEntry.getKey(), bclBiomes).getIntendedType();
            } else {
                type = typeFinder.find(biomeEntry.getKey(), type);
            }

            type = getBiomeType(includeMap, biomeEntry.getKey(), type);

            for (Map.Entry<BiomeAPI.BiomeType, BiomePicker> pickerEntry : acceptedBiomeTypes.entrySet()) {
                if (type.is(pickerEntry.getKey())) {
                    BCLBiome bclBiome;
                    if (foundBCLBiome) {
                        bclBiome = BCLBiomeRegistry.getBiome(biomeEntry.getKey(), bclBiomes);
                    } else {
                        //create and register a biome wrapper
                        bclBiome = new BCLBiome(biomeEntry.getKey().location(), type);
                        BCLBiomeRegistry.register(bclBiome);
                        foundBCLBiome = true;
                    }

                    boolean isPossible;
                    if (!bclBiome.hasParentBiome()) {
                        isPossible = pickerAdder.add(bclBiome, pickerEntry.getKey(), pickerEntry.getValue());
                    } else {
                        isPossible = true;
                    }

                    if (isPossible) {
                        allBiomes.add(biomes.getHolderOrThrow(biomeEntry.getKey()));
                    }
                }
            }
        }


        return allBiomes;
    }

    protected abstract BiomeAPI.BiomeType defaultBiomeType();
    protected abstract Map<BiomeAPI.BiomeType, BiomePicker> createFreshPickerMap();

    public abstract String toShortString();

    protected void onFinishBiomeRebuild(Map<BiomeAPI.BiomeType, BiomePicker> pickerMap) {
        for (var picker : pickerMap.values()) {
            picker.rebuild();
        }
    }

    protected final void rebuildBiomes(boolean force) {
        if (!force && didCreatePickers) return;

        if (Configs.MAIN_CONFIG.verboseLogging()) {
            BCLib.LOGGER.info("Updating Pickers for " + this.toShortString());
        }

        Map<BiomeAPI.BiomeType, BiomePicker> pickerMap = createFreshPickerMap();
        this.dynamicPossibleBiomes = populateBiomePickers(
                pickerMap,
                defaultBiomeType(),
                this::addToPicker,
                this::typeForUnknownBiome
        );
        if (this.dynamicPossibleBiomes == null) {
            this.dynamicPossibleBiomes = Set.of();
        } else {
            this.didCreatePickers = true;
        }

        onFinishBiomeRebuild(pickerMap);
    }

    @Override
    public BCLBiomeSource mergeWithBiomeSource(BiomeSource inputBiomeSource) {
        final RegistryAccess access = WorldBootstrap.getLastRegistryAccess();
        if (access == null) {
            BCLib.LOGGER.error("Unable to merge Biomesources!");
            return this;
        }

        final Map<BiomeAPI.BiomeType, List<String>> includeMap = Configs.BIOMES_CONFIG.getBiomeIncludeMap();
        final List<String> excludeList = Configs.BIOMES_CONFIG.getExcludeMatching(defaultBiomeType());
        final Registry<BCLBiome> bclBiomes = access.registryOrThrow(BCLBiomeRegistry.BCL_BIOMES_REGISTRY);

        try {
            for (Holder<Biome> possibleBiome : inputBiomeSource.possibleBiomes()) {
                ResourceKey<Biome> key = possibleBiome.unwrapKey().orElse(null);
                if (key != null) {
                    //skip over all biomes that were excluded in the config
                    if (excludeList.contains(key.location())) continue;

                    //this is a biome that has no type entry => create a new one for the default type of this registry
                    if (!BCLBiomeRegistry.hasBiome(key, bclBiomes)) {
                        BiomeAPI.BiomeType type = typeForUnknownBiome(key, defaultBiomeType());

                        //check if there was an override defined in the configs
                        type = getBiomeType(includeMap, key, type);

                        //create and register a biome wrapper
                        BCLBiome bclBiome = new BCLBiome(key.location(), type);
                        BCLBiomeRegistry.register(bclBiome);
                    }
                }
            }
        } catch (RuntimeException e) {
            BCLib.LOGGER.error("Error while rebuilding Biomesources!", e);
        } catch (Exception e) {
            BCLib.LOGGER.error("Error while rebuilding Biomesources!", e);
        }

        this.reloadBiomes();
        return this;
    }

    private static BiomeAPI.BiomeType getBiomeType(
            Map<BiomeAPI.BiomeType, List<String>> includeMap,
            ResourceKey<Biome> biomeKey,
            BiomeAPI.BiomeType defaultType
    ) {
        for (Map.Entry<BiomeAPI.BiomeType, List<String>> includeList : includeMap.entrySet()) {
            if (includeList.getValue().contains(biomeKey.location().toString())) {
                return includeList.getKey();
            }
        }


        return defaultType;
    }

    public void onLoadGeneratorSettings(NoiseGeneratorSettings generator) {
        this.setMaxHeight(generator.noiseSettings().height());
    }

    protected void reloadBiomes(boolean force) {
        rebuildBiomes(force);
        this.initMap(currentSeed);
    }

    @Override
    public void reloadBiomes() {
        reloadBiomes(true);
    }
}
