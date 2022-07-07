package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.worlds.together.biomesource.BiomeSourceWithConfig;
import org.betterx.worlds.together.biomesource.ReloadableBiomeSource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

public class BCLibEndBiomeSource extends BCLBiomeSource implements BiomeSourceWithConfig<BCLibEndBiomeSource, BCLEndBiomeSourceConfig>, ReloadableBiomeSource {
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
    private final Point pos;
    private final BiFunction<Point, Integer, Boolean> endLandFunction;
    private BiomeMap mapLand;
    private BiomeMap mapVoid;
    private BiomeMap mapCenter;
    private BiomeMap mapBarrens;

    private BiomePicker endLandBiomePicker;
    private BiomePicker endVoidBiomePicker;
    private BiomePicker endCenterBiomePicker;
    private BiomePicker endBarrensBiomePicker;

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
        rebuildBiomePickers();

        this.endLandFunction = GeneratorOptions.getEndLandFunction();
        this.pos = new Point();

        if (initMaps) {
            initMap(seed);
        }
    }

    @NotNull
    private void rebuildBiomePickers() {
        var includeMap = Configs.BIOMES_CONFIG.getBiomeIncludeMap();
        var excludeList = Configs.BIOMES_CONFIG.getExcludeMatching(BiomeAPI.BiomeType.END);

        this.endLandBiomePicker = new BiomePicker(biomeRegistry);
        this.endVoidBiomePicker = new BiomePicker(biomeRegistry);
        this.endCenterBiomePicker = new BiomePicker(biomeRegistry);
        this.endBarrensBiomePicker = new BiomePicker(biomeRegistry);
        Map<BiomeAPI.BiomeType, BiomePicker> pickerMap = new HashMap<>();
        pickerMap.put(BiomeAPI.BiomeType.END_LAND, endLandBiomePicker);
        pickerMap.put(BiomeAPI.BiomeType.END_VOID, endVoidBiomePicker);
        pickerMap.put(BiomeAPI.BiomeType.END_CENTER, endCenterBiomePicker);
        pickerMap.put(BiomeAPI.BiomeType.END_BARRENS, endBarrensBiomePicker);


        this.possibleBiomes().forEach(biome -> {
            ResourceKey<Biome> key = biome.unwrapKey().orElseThrow();
            ResourceLocation biomeID = key.location();
            String biomeStr = biomeID.toString();
            //exclude everything that was listed
            if (excludeList != null && excludeList.contains(biomeStr)) return;
            if (!biome.isBound()) {
                BCLib.LOGGER.warning("Biome " + biomeStr + " is requested but not yet bound.");
                return;
            }
            final BCLBiome bclBiome;
            if (!BiomeAPI.hasBiome(biomeID)) {
                bclBiome = new BCLBiome(biomeID, biome.value());
            } else {
                bclBiome = BiomeAPI.getBiome(biomeID);
            }


            if (bclBiome != null || bclBiome != BCLBiomeRegistry.EMPTY_BIOME) {
                if (bclBiome.getParentBiome() == null) {
                    //ignore small islands when void biomes are disabled
                    if (!config.withVoidBiomes) {
                        if (biomeID.equals(Biomes.SMALL_END_ISLANDS.location())) {
                            return;
                        }
                    }

                    //force include biomes
                    boolean didForceAdd = false;
                    for (var entry : pickerMap.entrySet()) {
                        var includeList = includeMap == null ? null : includeMap.get(entry.getKey());
                        if (includeList != null && includeList.contains(biomeStr)) {
                            entry.getValue().addBiome(bclBiome);
                            didForceAdd = true;
                        }
                    }

                    if (!didForceAdd) {
                        if (BiomeAPI.wasRegisteredAs(biomeID, BiomeAPI.BiomeType.END_IGNORE)) {
                            //we should not add this biome anywhere, so just ignore it
                        } else if (BiomeAPI.wasRegisteredAs(biomeID, BiomeAPI.BiomeType.END_CENTER)
                                || TheEndBiomesHelper.canGenerateAsMainIslandBiome(key)) {
                            endCenterBiomePicker.addBiome(bclBiome);
                        } else if (BiomeAPI.wasRegisteredAs(biomeID, BiomeAPI.BiomeType.END_LAND)
                                || TheEndBiomesHelper.canGenerateAsHighlandsBiome(key)) {
                            if (!config.withVoidBiomes) endVoidBiomePicker.addBiome(bclBiome);
                            endLandBiomePicker.addBiome(bclBiome);
                        } else if (BiomeAPI.wasRegisteredAs(biomeID, BiomeAPI.BiomeType.END_BARRENS)
                                || TheEndBiomesHelper.canGenerateAsEndBarrens(key)) {
                            endBarrensBiomePicker.addBiome(bclBiome);
                        } else if (BiomeAPI.wasRegisteredAs(biomeID, BiomeAPI.BiomeType.END_VOID)
                                || TheEndBiomesHelper.canGenerateAsSmallIslandsBiome(key)) {
                            endVoidBiomePicker.addBiome(bclBiome);
                        } else {
                            BCLib.LOGGER.info("Found End Biome " + biomeStr + " that was not registers with fabric or bclib. Assuming end-land Biome...");
                            endLandBiomePicker.addBiome(bclBiome);
                        }
                    }
                }
            }
        });

        endLandBiomePicker.rebuild();
        endVoidBiomePicker.rebuild();
        endBarrensBiomePicker.rebuild();
        endCenterBiomePicker.rebuild();

        if (endVoidBiomePicker.isEmpty()) {
            BCLib.LOGGER.info("No Void Biomes found. Disabling by using barrens");
            endVoidBiomePicker = endBarrensBiomePicker;
        }
        if (endBarrensBiomePicker.isEmpty()) {
            BCLib.LOGGER.info("No Barrens Biomes found. Disabling by using land Biomes");
            endBarrensBiomePicker = endLandBiomePicker;
            endVoidBiomePicker = endLandBiomePicker;
        }
        if (endCenterBiomePicker.isEmpty()) {
            BCLib.LOGGER.warning("No Center Island Biomes found. Forcing use of vanilla center.");
            endCenterBiomePicker.addBiome(BiomeAPI.THE_END);
            endCenterBiomePicker.rebuild();
            if (endCenterBiomePicker.isEmpty()) {
                BCLib.LOGGER.error("Unable to force vanilla central Island. Falling back to land Biomes...");
                endCenterBiomePicker = endLandBiomePicker;
            }
        }
    }

    protected BCLBiomeSource cloneForDatapack(Set<Holder<Biome>> datapackBiomes) {
        datapackBiomes.addAll(getBclBiomes(this.biomeRegistry));
        return new BCLibEndBiomeSource(
                this.biomeRegistry,
                datapackBiomes.stream()
                              .filter(b -> b.unwrapKey().orElse(null) != BCLBiomeRegistry.EMPTY_BIOME.getBiomeKey())
                              .toList(),
                this.currentSeed,
                this.config,
                true
        );
    }

    private static List<Holder<Biome>> getBclBiomes(Registry<Biome> biomeRegistry) {
        return getBiomes(
                biomeRegistry,
                Configs.BIOMES_CONFIG.getExcludeMatching(BiomeAPI.BiomeType.END),
                Configs.BIOMES_CONFIG.getIncludeMatching(BiomeAPI.BiomeType.END),
                BCLibEndBiomeSource::isValidNonVanillaEndBiome
        );
    }

    private static List<Holder<Biome>> getBiomes(Registry<Biome> biomeRegistry) {
        return getBiomes(
                biomeRegistry,
                Configs.BIOMES_CONFIG.getExcludeMatching(BiomeAPI.BiomeType.END),
                Configs.BIOMES_CONFIG.getIncludeMatching(BiomeAPI.BiomeType.END),
                BCLibEndBiomeSource::isValidEndBiome
        );
    }


    private static boolean isValidEndBiome(Holder<Biome> biome, ResourceLocation location) {
        if (BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.END_IGNORE)) return false;

        return biome.is(BiomeTags.IS_END) ||
                BiomeAPI.wasRegisteredAsEndBiome(location) ||
                TheEndBiomesHelper.canGenerateInEnd(biome.unwrapKey().orElse(null));
    }

    private static boolean isValidNonVanillaEndBiome(Holder<Biome> biome, ResourceLocation location) {
        if (BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.END_IGNORE)) return false;

        return biome.is(BiomeTags.IS_END) ||
                BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.BCL_END_LAND) ||
                BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.BCL_END_VOID) ||
                BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.BCL_END_CENTER) ||
                BiomeAPI.wasRegisteredAs(location, BiomeAPI.BiomeType.BCL_END_BARRENS) ||
                TheEndBiomesHelper.canGenerateInEnd(biome.unwrapKey().orElse(null));
    }

    public static void register() {
        Registry.register(Registry.BIOME_SOURCE, BCLib.makeID("end_biome_source"), CODEC);
    }

    @Override
    protected void onInitMap(long seed) {
        this.mapLand = config.mapVersion.mapBuilder.apply(
                seed,
                config.landBiomesSize,
                endLandBiomePicker
        );

        this.mapVoid = config.mapVersion.mapBuilder.apply(
                seed,
                config.voidBiomesSize,
                endVoidBiomePicker
        );

        this.mapCenter = config.mapVersion.mapBuilder.apply(
                seed,
                config.centerBiomesSize,
                endCenterBiomePicker
        );

        this.mapBarrens = config.mapVersion.mapBuilder.apply(
                seed,
                config.barrensBiomesSize,
                endBarrensBiomePicker
        );
    }

    @Override
    protected void onHeightChange(int newHeight) {

    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.@NotNull Sampler sampler) {
        if (mapLand == null || mapVoid == null || mapCenter == null || mapBarrens == null)
            return this.possibleBiomes().stream().findFirst().orElseThrow();

        int posX = QuartPos.toBlock(biomeX);
        int posY = QuartPos.toBlock(biomeY);
        int posZ = QuartPos.toBlock(biomeZ);

        long dist = Math.abs(posX) + Math.abs(posZ) > (long) config.innerVoidRadiusSquared
                ? ((long) config.innerVoidRadiusSquared + 1)
                : (long) posX * (long) posX + (long) posZ * (long) posZ;


        if ((biomeX & 63) == 0 || (biomeZ & 63) == 0) {
            mapLand.clearCache();
            mapVoid.clearCache();
            mapCenter.clearCache();
            mapVoid.clearCache();
        }

        if (config.generatorVersion == BCLEndBiomeSourceConfig.EndBiomeGeneratorType.VANILLA || endLandFunction == null) {
            if (dist <= (long) config.innerVoidRadiusSquared) {
                return mapCenter.getBiome(posX, biomeY << 2, posZ).biome;
            }
            int x = (SectionPos.blockToSectionCoord(posX) * 2 + 1) * 8;
            int z = (SectionPos.blockToSectionCoord(posZ) * 2 + 1) * 8;
            double d = sampler.erosion().compute(new DensityFunction.SinglePointContext(x, posY, z));
            if (d > 0.25) {
                return mapLand.getBiome(posX, biomeY << 2, posZ).biome; //highlands
            } else if (d >= -0.0625) {
                return mapLand.getBiome(posX, biomeY << 2, posZ).biome; //midlands
            } else {
                return d < -0.21875
                        ? mapVoid.getBiome(posX, biomeY << 2, posZ).biome //small islands
                        : (config.withVoidBiomes ? mapBarrens : mapLand).getBiome(
                                posX,
                                biomeY << 2,
                                posZ
                        ).biome; //barrens
            }
        } else {
            pos.setLocation(biomeX, biomeZ);
            if (endLandFunction.apply(pos, maxHeight)) {
                return (dist <= (long) config.innerVoidRadiusSquared ? mapCenter : mapLand)
                        .getBiome(posX, biomeY << 2, posZ).biome;
            } else {
                return (dist <= (long) config.innerVoidRadiusSquared ? mapBarrens : mapVoid)
                        .getBiome(posX, biomeY << 2, posZ).biome;
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

    @Override
    public void reloadBiomes() {
        rebuildBiomePickers();
        this.initMap(currentSeed);
    }
}
