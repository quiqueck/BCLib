package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.worlds.together.biomesource.BiomeSourceFromRegistry;
import org.betterx.worlds.together.biomesource.BiomeSourceHelper;
import org.betterx.worlds.together.biomesource.MergeableBiomeSource;
import org.betterx.worlds.together.world.BiomeSourceWithNoiseRelatedSettings;
import org.betterx.worlds.together.world.BiomeSourceWithSeed;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import com.google.common.collect.Sets;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public abstract class BCLBiomeSource extends BiomeSource implements BiomeSourceWithSeed, MergeableBiomeSource<BCLBiomeSource>, BiomeSourceWithNoiseRelatedSettings, BiomeSourceFromRegistry<BCLBiomeSource> {
    protected final Registry<Biome> biomeRegistry;
    private int registryModificationCounter;
    protected long currentSeed;
    protected int maxHeight;

    private static List<Holder<Biome>> preInit(Registry<Biome> biomeRegistry, List<Holder<Biome>> biomes) {
        biomes = biomes.stream().sorted(Comparator.comparing(holder -> holder.unwrapKey()
                                                                             .get()
                                                                             .location()
                                                                             .toString()))
                       .toList();
        return biomes;
    }

    protected BCLBiomeSource(
            Registry<Biome> biomeRegistry,
            List<Holder<Biome>> list,
            long seed
    ) {
        super(preInit(biomeRegistry, list));
        this.registryModificationCounter = InternalBiomeAPI.getBiomeRegistryModificationCount(biomeRegistry);
        this.biomeRegistry = biomeRegistry;
        this.currentSeed = seed;
    }

    @Override
    public boolean didBiomeRegistryChange() {
        return this.registryModificationCounter != InternalBiomeAPI.getBiomeRegistryModificationCount(biomeRegistry);
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

    public BCLBiomeSource createCopyForDatapack(Set<Holder<Biome>> datapackBiomes) {
        Set<Holder<Biome>> mutableSet = Sets.newHashSet();
        mutableSet.addAll(datapackBiomes);
        return cloneForDatapack(mutableSet);
    }

    protected abstract BCLBiomeSource cloneForDatapack(Set<Holder<Biome>> datapackBiomes);

    @NotNull
    protected String getNamespaces() {
        return BiomeSourceHelper.getNamespaces(possibleBiomes());
    }

    public interface ValidBiomePredicate {
        boolean isValid(Holder<Biome> biome, ResourceLocation location);
    }

    protected static List<Holder<Biome>> getBiomes(
            Registry<Biome> biomeRegistry,
            List<String> exclude,
            List<String> include,
            BCLibNetherBiomeSource.ValidBiomePredicate test
    ) {
        return biomeRegistry.stream()
                            .filter(biome -> biomeRegistry.getResourceKey(biome).isPresent())

                            .map(biome -> biomeRegistry.getOrCreateHolderOrThrow(biomeRegistry.getResourceKey(biome)
                                                                                              .get()))
                            .filter(biome -> {
                                ResourceLocation location = biome.unwrapKey().orElseThrow().location();
                                final String strLocation = location.toString();
                                if (exclude.contains(strLocation)) return false;
                                if (include.contains(strLocation)) return true;

                                return test.isValid(biome, location);
                            })
                            .toList();
    }

    @Override
    public BCLBiomeSource mergeWithBiomeSource(BiomeSource inputBiomeSource) {
        final Set<Holder<Biome>> datapackBiomes = inputBiomeSource.possibleBiomes();
        return this.createCopyForDatapack(datapackBiomes);
    }

    public void onLoadGeneratorSettings(NoiseGeneratorSettings generator) {
        this.setMaxHeight(generator.noiseSettings().height());
    }

    public Registry<Biome> getBiomeRegistry() {
        return biomeRegistry;
    }
}
