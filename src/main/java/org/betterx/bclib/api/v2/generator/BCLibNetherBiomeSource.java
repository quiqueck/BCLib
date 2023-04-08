package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.MapBuilderFunction;
import org.betterx.bclib.api.v2.generator.map.MapStack;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.worlds.together.biomesource.BiomeSourceWithConfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import net.fabricmc.fabric.api.biome.v1.NetherBiomes;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    //    private void rebuildBiomePicker() {
//        if (WorldBootstrap.getLastRegistryAccess() == null) {
//            biomePicker = new BiomePicker(null);
//            return;
//        }
//        biomePicker = new BiomePicker(WorldBootstrap.getLastRegistryAccess().lookupOrThrow(Registries.BIOME));
//        Registry<BCLBiome> bclBiomeRegistry = WorldBootstrap.getLastRegistryAccess()
//                                                            .registryOrThrow(BCLBiomeRegistry.BCL_BIOMES_REGISTRY);
//        this.possibleBiomes().forEach(biome -> {
//            ResourceLocation biomeID = biome.unwrapKey().orElseThrow().location();
//            if (!biome.isBound()) {
//                BCLib.LOGGER.warning("Biome " + biomeID.toString() + " is requested but not yet bound.");
//                return;
//            }
//
//
//            if (!bclBiomeRegistry.containsKey(biomeID)) {
//                BCLBiome bclBiome = new BCLBiome(biomeID, BiomeAPI.BiomeType.NETHER);
//                InternalBiomeAPI.registerBCLBiomeData(bclBiome);
//                biomePicker.addBiome(bclBiome);
//            } else {
//                BCLBiome bclBiome = bclBiomeRegistry.get(biomeID);
//
//                if (!BCLBiomeRegistry.isEmptyBiome(bclBiome)) {
//                    if (bclBiome.getParentBiome() == null) {
//                        biomePicker.addBiome(bclBiome);
//                    }
//                }
//            }
//        });
//
//        biomePicker.rebuild();
//    }

    protected BCLBiomeSource cloneForDatapack(Set<Holder<Biome>> datapackBiomes) {
//        datapackBiomes.addAll(getNonVanillaBiomes(this.biomeRegistry, this.bclBiomeRegistry));
//        datapackBiomes.addAll(possibleBiomes().stream()
//                                              .filter(h -> !h.unwrapKey()
//                                                             .orElseThrow()
//                                                             .location()
//                                                             .getNamespace()
//                                                             .equals("minecraft"))
//                                              .toList());
//        return new BCLibNetherBiomeSource(
//                datapackBiomes.stream()
//                              .filter(b -> b.unwrapKey()
//                                            .orElse(null) != BCLBiomeRegistry.EMPTY_BIOME.getBiomeKey())
//                              .toList(),
//                this.currentSeed,
//                config,
//                true
//        );
        return null;
    }

    private static List<Holder<Biome>> getNonVanillaBiomes(
            HolderGetter<Biome> biomeRegistry,
            HolderGetter<BCLBiome> bclBiomeRegistry
    ) {
//        List<String> include = Configs.BIOMES_CONFIG.getIncludeMatching(BiomeAPI.BiomeType.NETHER);
//        List<String> exclude = Configs.BIOMES_CONFIG.getExcludeMatching(BiomeAPI.BiomeType.NETHER);
//
//        return getBiomes(
//                biomeRegistry,
//                bclBiomeRegistry,
//                exclude,
//                include,
//                BCLibNetherBiomeSource::isValidNonVanillaNetherBiome
//        );
        return null;
    }


    private static List<Holder<Biome>> getBiomes(
            HolderGetter<Biome> biomeRegistry,
            HolderGetter<BCLBiome> bclBiomeRegistry
    ) {
//        List<String> include = Configs.BIOMES_CONFIG.getIncludeMatching(BiomeAPI.BiomeType.NETHER);
//        List<String> exclude = Configs.BIOMES_CONFIG.getExcludeMatching(BiomeAPI.BiomeType.NETHER);
//
//        return getBiomes(biomeRegistry, bclBiomeRegistry, exclude, include, BCLibNetherBiomeSource::isValidNetherBiome);
        return null;
    }


    private static boolean isValidNetherBiome(Holder<Biome> biome, ResourceLocation location) {
        return NetherBiomes.canGenerateInNether(biome.unwrapKey().get()) ||
                biome.is(BiomeTags.IS_NETHER) ||
                BiomeAPI.wasRegisteredAsNetherBiome(location);
    }

    private static boolean isValidNonVanillaNetherBiome(Holder<Biome> biome, ResourceLocation location) {
        if (BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.END_IGNORE) || biome.unwrapKey()
                                                                                      .orElseThrow()
                                                                                      .location()
                                                                                      .getNamespace()
                                                                                      .equals("minecraft"))
            return false;

        return NetherBiomes.canGenerateInNether(biome.unwrapKey().get()) ||
                BiomeAPI.wasRegisteredAsNetherBiome(location);
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
