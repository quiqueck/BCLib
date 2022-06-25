package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.map.MapStack;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.config.ConfigKeeper.StringArrayEntry;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.bclib.util.TriFunction;
import org.betterx.worlds.together.biomesource.BiomeSourceWithConfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import net.fabricmc.fabric.api.biome.v1.NetherBiomes;

import java.util.List;
import java.util.Set;

public class BCLibNetherBiomeSource extends BCLBiomeSource implements BiomeSourceWithConfig<BCLibNetherBiomeSource, BCLNetherBiomeSourceConfig> {
    public static final Codec<BCLibNetherBiomeSource> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(
                            RegistryOps
                                    .retrieveRegistry(Registry.BIOME_REGISTRY)
                                    .forGetter(source -> source.biomeRegistry),
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
    private final BiomePicker biomePicker;
    private BCLNetherBiomeSourceConfig config;

    public BCLibNetherBiomeSource(Registry<Biome> biomeRegistry, BCLNetherBiomeSourceConfig config) {
        this(biomeRegistry, 0, config, false);
    }

    public BCLibNetherBiomeSource(Registry<Biome> biomeRegistry, long seed, BCLNetherBiomeSourceConfig config) {
        this(biomeRegistry, seed, config, true);
    }

    private BCLibNetherBiomeSource(
            Registry<Biome> biomeRegistry,
            long seed,
            BCLNetherBiomeSourceConfig config,
            boolean initMaps
    ) {
        this(biomeRegistry, getBiomes(biomeRegistry), seed, config, initMaps);
    }

    private BCLibNetherBiomeSource(
            Registry<Biome> biomeRegistry,
            List<Holder<Biome>> list,
            long seed,
            BCLNetherBiomeSourceConfig config,
            boolean initMaps
    ) {
        super(biomeRegistry, list, seed);
        this.config = config;
        biomePicker = new BiomePicker(biomeRegistry);

        this.possibleBiomes().forEach(biome -> {
            ResourceLocation key = biome.unwrapKey().orElseThrow().location();

            if (!BiomeAPI.hasBiome(key)) {
                BCLBiome bclBiome = new BCLBiome(key, biome.value());
                biomePicker.addBiome(bclBiome);
            } else {
                BCLBiome bclBiome = BiomeAPI.getBiome(key);

                if (bclBiome != BiomeAPI.EMPTY_BIOME) {
                    if (bclBiome.getParentBiome() == null) {
                        biomePicker.addBiome(bclBiome);
                    }
                }
            }
        });

        biomePicker.rebuild();
        if (initMaps) {
            initMap(seed);
        }
    }

    protected BCLBiomeSource cloneForDatapack(Set<Holder<Biome>> datapackBiomes) {
        datapackBiomes.addAll(getBclBiomes(this.biomeRegistry));
        return new BCLibNetherBiomeSource(
                this.biomeRegistry,
                datapackBiomes.stream().toList(),
                this.currentSeed,
                config,
                true
        );
    }

    private static List<Holder<Biome>> getBclBiomes(Registry<Biome> biomeRegistry) {
        List<String> include = Configs.BIOMES_CONFIG.getEntry("force_include", "nether_biomes", StringArrayEntry.class)
                                                    .getValue();
        List<String> exclude = Configs.BIOMES_CONFIG.getEntry("force_exclude", "nether_biomes", StringArrayEntry.class)
                                                    .getValue();

        return getBiomes(biomeRegistry, exclude, include, BCLibNetherBiomeSource::isValidNonVanillaNetherBiome);
    }


    private static List<Holder<Biome>> getBiomes(Registry<Biome> biomeRegistry) {
        List<String> include = Configs.BIOMES_CONFIG.getEntry("force_include", "nether_biomes", StringArrayEntry.class)
                                                    .getValue();
        List<String> exclude = Configs.BIOMES_CONFIG.getEntry("force_exclude", "nether_biomes", StringArrayEntry.class)
                                                    .getValue();

        return getBiomes(biomeRegistry, exclude, include, BCLibNetherBiomeSource::isValidNetherBiome);
    }


    private static boolean isValidNetherBiome(Holder<Biome> biome, ResourceLocation location) {
        return biome.unwrapKey().get().location().toString().contains("gravel_desert");

//        return NetherBiomes.canGenerateInNether(biome.unwrapKey().get()) ||
//                biome.is(BiomeTags.IS_NETHER) ||
//                BiomeAPI.wasRegisteredAsNetherBiome(location);
    }

    private static boolean isValidNonVanillaNetherBiome(Holder<Biome> biome, ResourceLocation location) {
        return (
                !"minecraft".equals(location.getNamespace()) &&
                        NetherBiomes.canGenerateInNether(biome.unwrapKey().get())) ||
                BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.BCL_NETHER);
    }

    public static <T> void debug(Object el, Registry<T> reg) {
        System.out.println("Unknown " + el + " in " + reg);
    }

    public static void register() {
        Registry.register(Registry.BIOME_SOURCE, BCLib.makeID("nether_biome_source"), CODEC);
    }


    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler var4) {
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
        TriFunction<Long, Integer, BiomePicker, BiomeMap> mapConstructor = config.mapVersion.mapBuilder;
        if (maxHeight > 128 && GeneratorOptions.useVerticalBiomes()) {
            this.biomeMap = new MapStack(
                    seed,
                    GeneratorOptions.getBiomeSizeNether(),
                    biomePicker,
                    GeneratorOptions.getVerticalBiomeSizeNether(),
                    maxHeight,
                    mapConstructor
            );
        } else {
            this.biomeMap = mapConstructor.apply(
                    seed,
                    GeneratorOptions.getBiomeSizeNether(),
                    biomePicker
            );
        }
    }

    @Override
    protected void onHeightChange(int newHeight) {
        initMap(currentSeed);
    }

    @Override
    public String toString() {
        return "BCLib - Nether BiomeSource (" + Integer.toHexString(hashCode()) + ", config=" + config + ", seed=" + currentSeed + ", height=" + maxHeight + ", biomes=" + possibleBiomes().size() + ")";
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
