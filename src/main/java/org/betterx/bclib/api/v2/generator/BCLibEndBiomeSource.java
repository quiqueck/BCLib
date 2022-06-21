package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.map.hex.HexBiomeMap;
import org.betterx.bclib.api.v2.generator.map.square.SquareBiomeMap;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.config.ConfigKeeper.StringArrayEntry;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.bclib.interfaces.TheEndBiomeDataAccessor;
import org.betterx.bclib.noise.OpenSimplexNoise;
import org.betterx.bclib.presets.worldgen.BCLWorldPresetSettings;
import org.betterx.worlds.together.world.WorldGenUtil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import net.fabricmc.fabric.impl.biome.TheEndBiomeData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

public class BCLibEndBiomeSource extends BCLBiomeSource {
    private static final OpenSimplexNoise SMALL_NOISE = new OpenSimplexNoise(8324);
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
                                                                      Codec
                                                                              .INT
                                                                              .optionalFieldOf("version")
                                                                              .stable()
                                                                              .forGetter(source -> Optional.of(source.biomeSourceVersion))
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
    private SimplexNoise noise;
    private BiomeMap mapLand;
    private BiomeMap mapVoid;

    private final BiomePicker endLandBiomePicker;
    private final BiomePicker endVoidBiomePicker;

    private boolean generateEndVoids;

    public BCLibEndBiomeSource(Registry<Biome> biomeRegistry, long seed, Optional<Integer> version) {
        this(biomeRegistry, seed, version, true);
    }

    public BCLibEndBiomeSource(Registry<Biome> biomeRegistry, Optional<Integer> version) {
        this(biomeRegistry, 0, version, false);
    }

    private BCLibEndBiomeSource(Registry<Biome> biomeRegistry, long seed, Optional<Integer> version, boolean initMaps) {
        this(biomeRegistry, getBiomes(biomeRegistry), seed, version, initMaps);
    }

    private BCLibEndBiomeSource(
            Registry<Biome> biomeRegistry,
            List<Holder<Biome>> list,
            long seed,
            Optional<Integer> version,
            boolean initMaps
    ) {
        super(biomeRegistry, list, seed, version);
        if (WorldGenUtil.getWorldSettings() instanceof BCLWorldPresetSettings settings) {
            generateEndVoids = settings.generateEndVoid;
        } else {
            generateEndVoids = true;
        }

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
                        if (generateEndVoids) {
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
                            if (!key.equals(Biomes.SMALL_END_ISLANDS) && !key.equals(Biomes.THE_END)
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

        if (WorldGenUtil.getWorldSettings() instanceof BCLWorldPresetSettings settings
                && !settings.useEndTerrainGenerator) {
            this.endLandFunction = null;
        } else {
            this.endLandFunction = GeneratorOptions.getEndLandFunction();
        }
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
                Optional.of(biomeSourceVersion),
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


    public static float getLegacyHeightValue(SimplexNoise simplexNoise, int i, int j) {
        int k = i / 2;
        int l = j / 2;
        int m = i % 2;
        int n = j % 2;
        float f = 100.0f - Mth.sqrt(i * i + j * j) * 8.0f;
        f = Mth.clamp(f, -100.0f, 80.0f);
        for (int o = -12; o <= 12; ++o) {
            for (int p = -12; p <= 12; ++p) {
                long q = k + o;
                long r = l + p;
                if (q * q + r * r <= 4096L || !(simplexNoise.getValue(q, r) < (double) -0.9f)) continue;
                float g = (Mth.abs(q) * 3439.0f + Mth.abs(r) * 147.0f) % 13.0f + 9.0f;
                float h = m - o * 2;
                float s = n - p * 2;
                float t = 100.0f - Mth.sqrt(h * h + s * s) * g;
                t = Mth.clamp(t, -100.0f, 80.0f);
                f = Math.max(f, t);
            }
        }
        return f;
    }

    public static void register() {
        Registry.register(Registry.BIOME_SOURCE, BCLib.makeID("end_biome_source"), CODEC);
    }

    @Override
    protected void onInitMap(long seed) {
        if ((biomeSourceVersion != BIOME_SOURCE_VERSION_HEX)) {
            this.mapLand = new SquareBiomeMap(
                    seed,
                    GeneratorOptions.getBiomeSizeEndLand(),
                    endLandBiomePicker
            );
            this.mapVoid = new SquareBiomeMap(
                    seed,
                    GeneratorOptions.getBiomeSizeEndVoid(),
                    endVoidBiomePicker
            );
        } else {
            this.mapLand = new HexBiomeMap(
                    seed,
                    GeneratorOptions.getBiomeSizeEndLand(),
                    endLandBiomePicker
            );
            this.mapVoid = new HexBiomeMap(
                    seed,
                    GeneratorOptions.getBiomeSizeEndVoid(),
                    endVoidBiomePicker
            );
        }

        WorldgenRandom chunkRandom = new WorldgenRandom(new LegacyRandomSource(seed));
        chunkRandom.consumeCount(17292);
        this.noise = new SimplexNoise(chunkRandom);
    }

    @Override
    protected void onHeightChange(int newHeight) {

    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
        if (mapLand == null || mapVoid == null)
            return this.possibleBiomes().stream().findFirst().get();

        int posX = QuartPos.toBlock(biomeX);
        int posY = QuartPos.toBlock(biomeY);
        int posZ = QuartPos.toBlock(biomeZ);
        long farEndBiomes = GeneratorOptions.getFarEndBiomes();

        long dist = posX * posX + posZ * posZ;

        if ((biomeX & 63) == 0 && (biomeZ & 63) == 0) {
            mapLand.clearCache();
            mapVoid.clearCache();
        }

        if (endLandFunction == null) {
            if (dist <= farEndBiomes) {
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
                        : generateEndVoids ? this.barrens : mapVoid.getBiome(posX, biomeY << 2, posZ).biome;
            }
        } else {
            pos.setLocation(biomeX, biomeZ);
            if (endLandFunction.apply(pos, maxHeight)) {
                return dist <= farEndBiomes ? centerBiome : mapLand.getBiome(posX, biomeY << 2, posZ).biome;
            } else {
                return dist <= farEndBiomes
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
        return "BCLib - The End BiomeSource (" + Integer.toHexString(hashCode()) + ", version=" + biomeSourceVersion + ", seed=" + currentSeed + ", height=" + maxHeight + ", voids=" + generateEndVoids + ", customLand=" + (endLandFunction != null) + ", biomes=" + possibleBiomes().size() + ")";
    }
}
