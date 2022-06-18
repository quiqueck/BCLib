package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.map.MapStack;
import org.betterx.bclib.api.v2.generator.map.hex.HexBiomeMap;
import org.betterx.bclib.api.v2.generator.map.square.SquareBiomeMap;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.config.ConfigKeeper.StringArrayEntry;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.interfaces.BiomeMap;

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

import org.apache.commons.lang3.function.TriFunction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BCLibNetherBiomeSource extends BCLBiomeSource {
    private static int lastWorldHeight;
    private static int worldHeight;
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
                            Codec
                                    .INT
                                    .optionalFieldOf("version")
                                    .stable()
                                    .forGetter(source -> Optional.of(source.biomeSourceVersion))
                    )
                    .apply(instance, instance.stable(BCLibNetherBiomeSource::new))
            );
    private BiomeMap biomeMap;
    private final BiomePicker biomePicker;

    public BCLibNetherBiomeSource(Registry<Biome> biomeRegistry, Optional<Integer> version) {
        this(biomeRegistry, 0, version, false);
    }

    public BCLibNetherBiomeSource(Registry<Biome> biomeRegistry, long seed, Optional<Integer> version) {
        this(biomeRegistry, seed, version, true);
    }

    private BCLibNetherBiomeSource(
            Registry<Biome> biomeRegistry,
            long seed,
            Optional<Integer> version,
            boolean initMaps
    ) {
        this(biomeRegistry, getBiomes(biomeRegistry), seed, version, initMaps);
    }

    private BCLibNetherBiomeSource(
            Registry<Biome> biomeRegistry,
            List<Holder<Biome>> list,
            long seed,
            Optional<Integer> version,
            boolean initMaps
    ) {
        super(biomeRegistry, list, seed, version);

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
                Optional.of(biomeSourceVersion),
                true
        );
    }

    /**
     * Set world height, used when Nether is larger than vanilla 128 blocks tall.
     *
     * @param worldHeight height of the Nether ceiling.
     */
    public static void setWorldHeight(int worldHeight) {
        BCLibNetherBiomeSource.worldHeight = worldHeight;
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
        return biome.unwrapKey().get().location().toString().equals("betternether:crimson_glowing_woods")
                || biome.unwrapKey().get().location().toString().equals("minecraft:crimson_forest")
                || biome.unwrapKey().get().location().toString().equals("betternether:crimson_pinewood");

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
        if (lastWorldHeight != worldHeight) {
            lastWorldHeight = worldHeight;
            initMap(this.currentSeed);
        }
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
        TriFunction<Long, Integer, BiomePicker, BiomeMap> mapConstructor = (biomeSourceVersion != BIOME_SOURCE_VERSION_HEX)
                ? SquareBiomeMap::new
                : HexBiomeMap::new;
        if (worldHeight > 128 && GeneratorOptions.useVerticalBiomes()) {
            this.biomeMap = new MapStack(
                    seed,
                    GeneratorOptions.getBiomeSizeNether(),
                    biomePicker,
                    GeneratorOptions.getVerticalBiomeSizeNether(),
                    worldHeight,
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
    public String toString() {
        return "BCLib - Nether BiomeSource (" + Integer.toHexString(hashCode()) + ", version=" + biomeSourceVersion + ", seed=" + currentSeed + ", biomes=" + possibleBiomes().size() + ")";
    }
}
