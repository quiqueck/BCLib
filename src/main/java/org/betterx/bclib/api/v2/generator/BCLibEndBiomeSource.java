package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.config.ConfigKeeper.StringArrayEntry;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.bclib.interfaces.TheEndBiomeDataAccessor;
import org.betterx.worlds.together.biomesource.BiomeSourceWithConfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

import net.fabricmc.fabric.impl.biome.TheEndBiomeData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

public class BCLibEndBiomeSource extends BCLBiomeSource implements BiomeSourceWithConfig<BCLibEndBiomeSource, BCLEndBiomeSourceConfig> {
    public static Codec<BCLibEndBiomeSource> CODEC
            = RecordCodecBuilder.create((instance) -> instance.group(
                                                                      RegistryOps
                                                                              .retrieveRegistry(Registry.BIOME_REGISTRY)
                                                                              .forGetter((theEndBiomeSource) -> theEndBiomeSource.biomeRegistry),
                                                                      Codec
                                                                              .LONG
                                                                              .fieldOf("seed")
                                                                              .stable()
                                                                              .forGetter(source -> source.currentSeed),
                                                                      BCLEndBiomeSourceConfig
                                                                              .CODEC
                                                                              .fieldOf("config")
                                                                              .orElse(BCLEndBiomeSourceConfig.DEFAULT)
                                                                              .forGetter(o -> o.config)
                                                              )
                                                              .apply(
                                                                      instance,
                                                                      instance.stable(BCLibEndBiomeSource::new)
                                                              )
    );
    private final Holder<Biome> centerBiome;
    private final Holder<Biome> barrens;
    private final Point pos;
    private final BiFunction<Point, Integer, Boolean> endLandFunction;
    private BiomeMap mapLand;
    private BiomeMap mapVoid;

    private final BiomePicker endLandBiomePicker;
    private final BiomePicker endVoidBiomePicker;

    private BCLEndBiomeSourceConfig config;

    public BCLibEndBiomeSource(Registry<Biome> biomeRegistry, long seed, BCLEndBiomeSourceConfig config) {
        this(biomeRegistry, seed, config, true);
    }

    public BCLibEndBiomeSource(Registry<Biome> biomeRegistry, BCLEndBiomeSourceConfig config) {
        this(biomeRegistry, 0, config, false);
    }

    private BCLibEndBiomeSource(
            Registry<Biome> biomeRegistry,
            long seed,
            BCLEndBiomeSourceConfig config,
            boolean initMaps
    ) {
        this(biomeRegistry, getBiomes(biomeRegistry), seed, config, initMaps);
    }

    private BCLibEndBiomeSource(
            Registry<Biome> biomeRegistry,
            List<Holder<Biome>> list,
            long seed,
            BCLEndBiomeSourceConfig config,
            boolean initMaps
    ) {
        super(biomeRegistry, list, seed);
        this.config = config;

        endLandBiomePicker = new BiomePicker(biomeRegistry);
        endVoidBiomePicker = new BiomePicker(biomeRegistry);

        List<String> includeVoid = Configs.BIOMES_CONFIG.getEntry(
                "force_include",
                "end_void_biomes",
                StringArrayEntry.class
        ).getValue();

        List<String> includeLand = Configs.BIOMES_CONFIG.getEntry(
                "force_include",
                "end_land_biomes",
                StringArrayEntry.class
        ).getValue();
        this.possibleBiomes().forEach(biome -> {
            ResourceLocation key = biome.unwrapKey().orElseThrow().location();


            if (!BiomeAPI.hasBiome(key)) {
                BCLBiome bclBiome = new BCLBiome(key, biome.value());

                if (includeVoid.contains(key.toString())) {
                    endVoidBiomePicker.addBiome(bclBiome);
                } else {
                    endLandBiomePicker.addBiome(bclBiome);
                }
            } else {
                BCLBiome bclBiome = BiomeAPI.getBiome(key);
                if (bclBiome != BiomeAPI.EMPTY_BIOME) {
                    if (bclBiome.getParentBiome() == null) {
                        if (config.withVoidBiomes) {
                            if (BiomeAPI.wasRegisteredAsEndVoidBiome(key) || includeVoid.contains(key.toString())) {
                                endVoidBiomePicker.addBiome(bclBiome);
                            } else if (BiomeAPI.wasRegisteredAsEndLandBiome(key) || includeLand.contains(key.toString())) {
                                endLandBiomePicker.addBiome(bclBiome);
                            }
                        } else {
                            if (BiomeAPI.wasRegisteredAsEndLandBiome(key) || includeLand.contains(key.toString())) {
                                endLandBiomePicker.addBiome(bclBiome);
                                endVoidBiomePicker.addBiome(bclBiome);
                            }
                            if (!key.equals(Biomes.SMALL_END_ISLANDS.location()) && !key.equals(Biomes.THE_END.location())
                                    && (BiomeAPI.wasRegisteredAsEndVoidBiome(key) || includeVoid.contains(key.toString()))
                            ) {
                                endVoidBiomePicker.addBiome(bclBiome);
                            }

                        }
                    }
                }
            }
        });


        endLandBiomePicker.rebuild();
        endVoidBiomePicker.rebuild();


        this.centerBiome = biomeRegistry.getOrCreateHolderOrThrow(Biomes.THE_END);
        this.barrens = biomeRegistry.getOrCreateHolderOrThrow(Biomes.END_BARRENS);

        this.endLandFunction = GeneratorOptions.getEndLandFunction();
        this.pos = new Point();

        if (initMaps) {
            initMap(seed);
        }
    }

    protected BCLBiomeSource cloneForDatapack(Set<Holder<Biome>> datapackBiomes) {
        datapackBiomes.addAll(getBclBiomes(this.biomeRegistry));
        return new BCLibEndBiomeSource(
                this.biomeRegistry,
                datapackBiomes.stream().toList(),
                this.currentSeed,
                this.config,
                true
        );
    }

    private static List<Holder<Biome>> getBclBiomes(Registry<Biome> biomeRegistry) {
        List<String> include = Configs.BIOMES_CONFIG.getEntry(
                "force_include",
                "end_land_biomes",
                StringArrayEntry.class
        ).getValue();
        include.addAll(Configs.BIOMES_CONFIG.getEntry(
                "force_include",
                "end_void_biomes",
                StringArrayEntry.class
        ).getValue());
        if (TheEndBiomeData.createOverrides(biomeRegistry) instanceof TheEndBiomeDataAccessor acc) {
            return getBiomes(
                    biomeRegistry,
                    new ArrayList<>(0),
                    include,
                    (biome, location) ->
                            BCLibEndBiomeSource.isValidNonVanillaEndBiome(biome, location) ||
                                    acc.bcl_isNonVanillaAndCanGenerateInEnd(biome.unwrapKey().orElseThrow())

            );
        } else {
            return getBiomes(
                    biomeRegistry,
                    new ArrayList<>(0),
                    include,
                    BCLibEndBiomeSource::isValidNonVanillaEndBiome
            );
        }
    }

    private static List<Holder<Biome>> getBiomes(Registry<Biome> biomeRegistry) {
        List<String> include = Configs.BIOMES_CONFIG.getEntry(
                "force_include",
                "end_land_biomes",
                StringArrayEntry.class
        ).getValue();
        include.addAll(Configs.BIOMES_CONFIG.getEntry(
                "force_include",
                "end_void_biomes",
                StringArrayEntry.class
        ).getValue());

        if (TheEndBiomeData.createOverrides(biomeRegistry) instanceof TheEndBiomeDataAccessor acc) {
            return getBiomes(
                    biomeRegistry,
                    new ArrayList<>(0),
                    include,
                    (biome, location) ->
                            BCLibEndBiomeSource.isValidEndBiome(biome, location) || acc.bcl_canGenerateInEnd(
                                    biome.unwrapKey().orElseThrow())

            );
        } else {
            return getBiomes(biomeRegistry, new ArrayList<>(0), include, BCLibEndBiomeSource::isValidEndBiome);
        }
    }


    private static boolean isValidEndBiome(Holder<Biome> biome, ResourceLocation location) {
        return biome.is(BiomeTags.IS_END) ||
                BiomeAPI.wasRegisteredAsEndBiome(location);
    }

    private static boolean isValidNonVanillaEndBiome(Holder<Biome> biome, ResourceLocation location) {
        return biome.is(BiomeTags.IS_END) ||
                BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.BCL_END_LAND) ||
                BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.BCL_END_VOID);
    }

    public static void register() {
        Registry.register(Registry.BIOME_SOURCE, BCLib.makeID("end_biome_source"), CODEC);
    }

    @Override
    protected void onInitMap(long seed) {
        this.mapLand = config.mapVersion.mapBuilder.apply(
                seed,
                GeneratorOptions.getBiomeSizeEndLand(),
                endLandBiomePicker
        );

        this.mapVoid = config.mapVersion.mapBuilder.apply(
                seed,
                GeneratorOptions.getBiomeSizeEndVoid(),
                endVoidBiomePicker
        );
    }

    @Override
    protected void onHeightChange(int newHeight) {

    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.@NotNull Sampler sampler) {
        if (mapLand == null || mapVoid == null)
            return this.possibleBiomes().stream().findFirst().orElseThrow();

        int posX = QuartPos.toBlock(biomeX);
        int posY = QuartPos.toBlock(biomeY);
        int posZ = QuartPos.toBlock(biomeZ);

        long dist = Math.abs(posX) + Math.abs(posZ) > (long) config.innerVoidRadiusSquared
                ? ((long) config.innerVoidRadiusSquared + 1)
                : (long) posX * (long) posX + (long) posZ * (long) posZ;

        if ((biomeX & 63) == 0 && (biomeZ & 63) == 0) {
            mapLand.clearCache();
            mapVoid.clearCache();
        }

        if (config.generatorVersion == BCLEndBiomeSourceConfig.EndBiomeGeneratorType.VANILLA || endLandFunction == null) {
            if (dist <= (long) config.innerVoidRadiusSquared) {
                return this.centerBiome;
            }
            int x = (SectionPos.blockToSectionCoord(posX) * 2 + 1) * 8;
            int z = (SectionPos.blockToSectionCoord(posZ) * 2 + 1) * 8;
            double d = sampler.erosion().compute(new DensityFunction.SinglePointContext(x, posY, z));
            if (d > 0.25) {
                return mapLand.getBiome(posX, biomeY << 2, posZ).biome;
            } else if (d >= -0.0625) {
                return mapLand.getBiome(posX, biomeY << 2, posZ).biome;
            } else {
                return d < -0.21875
                        ? mapVoid.getBiome(posX, biomeY << 2, posZ).biome
                        : config.withVoidBiomes ? this.barrens : mapVoid.getBiome(posX, biomeY << 2, posZ).biome;
            }
        } else {
            pos.setLocation(biomeX, biomeZ);
            if (endLandFunction.apply(pos, maxHeight)) {
                return dist <= (long) config.innerVoidRadiusSquared
                        ? centerBiome : mapLand.getBiome(posX, biomeY << 2, posZ).biome;
            } else {
                return dist <= (long) config.innerVoidRadiusSquared
                        ? barrens
                        : mapVoid.getBiome(posX, biomeY << 2, posZ).biome;
            }
        }

    }


    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "BCLib - The End BiomeSource (" + Integer.toHexString(hashCode()) + ", config=" + config + ", seed=" + currentSeed + ", height=" + maxHeight + ", customLand=" + (endLandFunction != null) + ", biomes=" + possibleBiomes().size() + ")";
    }

    @Override
    public BCLEndBiomeSourceConfig getTogetherConfig() {
        return config;
    }

    @Override
    public void setTogetherConfig(BCLEndBiomeSourceConfig newConfig) {
        this.config = newConfig;
        this.initMap(currentSeed);
    }
}
