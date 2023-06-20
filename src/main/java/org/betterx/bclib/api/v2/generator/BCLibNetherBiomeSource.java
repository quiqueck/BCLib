package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.MapBuilderFunction;
import org.betterx.bclib.api.v2.generator.map.MapStack;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.worlds.together.biomesource.BiomeSourceWithConfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import net.fabricmc.fabric.api.biome.v1.NetherBiomes;

import java.util.Map;

public class BCLibNetherBiomeSource extends BCLBiomeSource implements BiomeSourceWithConfig<BCLibNetherBiomeSource, BCLNetherBiomeSourceConfig> {
    public static final Codec<BCLibNetherBiomeSource> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(
                            Codec
                                    .LONG
                                    .fieldOf("seed")
                                    .stable()
                                    .forGetter(source -> {
                                        return source.currentSeed;
                                    }),
                            BCLNetherBiomeSourceConfig
                                    .CODEC
                                    .fieldOf("config")
                                    .orElse(BCLNetherBiomeSourceConfig.DEFAULT)
                                    .forGetter(o -> o.config)
                    )
                    .apply(instance, instance.stable(BCLibNetherBiomeSource::new))
            );
    private BiomeMap biomeMap;
    private BiomePicker biomePicker;
    private BCLNetherBiomeSourceConfig config;

    public BCLibNetherBiomeSource(
            BCLNetherBiomeSourceConfig config
    ) {
        this(0, config, false);
    }

    private BCLibNetherBiomeSource(
            long seed,
            BCLNetherBiomeSourceConfig config
    ) {
        this(seed, config, true);
    }


    private BCLibNetherBiomeSource(
            long seed,
            BCLNetherBiomeSourceConfig config,
            boolean initMaps
    ) {
        super(seed);
        this.config = config;
        rebuildBiomes(false);
        if (initMaps) {
            initMap(seed);
        }
    }

    @Override
    protected BiomeAPI.BiomeType defaultBiomeType() {
        return BiomeAPI.BiomeType.NETHER;
    }

    @Override
    protected Map<BiomeAPI.BiomeType, BiomePicker> createFreshPickerMap() {
        this.biomePicker = new BiomePicker();
        return Map.of(defaultBiomeType(), this.biomePicker);
    }

    @Override
    protected BiomeAPI.BiomeType typeForUnknownBiome(ResourceKey<Biome> biomeKey, BiomeAPI.BiomeType defaultType) {
        //
        if (NetherBiomes.canGenerateInNether(biomeKey)) {
            return BiomeAPI.BiomeType.NETHER;
        }

        return super.typeForUnknownBiome(biomeKey, defaultType);
    }

    public static void register() {
        Registry.register(BuiltInRegistries.BIOME_SOURCE, BCLib.makeID("nether_biome_source"), CODEC);
    }
    
    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler var4) {
        if (!wasBound()) reloadBiomes(false);

        if (biomeMap == null)
            return this.possibleBiomes().stream().findFirst().get();

        if ((biomeX & 63) == 0 && (biomeZ & 63) == 0) {
            biomeMap.clearCache();
        }
        BiomePicker.ActualBiome bb = biomeMap.getBiome(biomeX << 2, biomeY << 2, biomeZ << 2);
        return bb.biome;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected void onInitMap(long seed) {
        MapBuilderFunction mapConstructor = config.mapVersion.mapBuilder;
        if (maxHeight > config.biomeSizeVertical * 1.5 && config.useVerticalBiomes) {
            this.biomeMap = new MapStack(
                    seed,
                    config.biomeSize,
                    biomePicker,
                    config.biomeSizeVertical,
                    maxHeight,
                    mapConstructor
            );
        } else {
            this.biomeMap = mapConstructor.create(
                    seed,
                    config.biomeSize,
                    biomePicker
            );
        }
    }

    @Override
    protected void onHeightChange(int newHeight) {
        initMap(currentSeed);
    }

    @Override
    public String toShortString() {
        return "BCLib - Nether BiomeSource (" + Integer.toHexString(hashCode()) + ")";
    }

    @Override
    public String toString() {
        return "\n" + toShortString() +
                "\n    biomes     = " + possibleBiomes().size() +
                "\n    namespaces = " + getNamespaces() +
                "\n    seed       = " + currentSeed +
                "\n    height     = " + maxHeight +
                "\n    config     = " + config;
    }

    @Override
    public BCLNetherBiomeSourceConfig getTogetherConfig() {
        return config;
    }

    @Override
    public void setTogetherConfig(BCLNetherBiomeSourceConfig newConfig) {
        this.config = newConfig;
        initMap(currentSeed);
    }
}
