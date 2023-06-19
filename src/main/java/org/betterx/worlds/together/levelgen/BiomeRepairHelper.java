package org.betterx.worlds.together.levelgen;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.worlds.together.biomesource.BiomeSourceWithConfig;
import org.betterx.worlds.together.biomesource.ReloadableBiomeSource;
import org.betterx.worlds.together.chunkgenerator.EnforceableChunkGenerator;
import org.betterx.worlds.together.tag.v3.CommonBiomeTags;
import org.betterx.worlds.together.world.event.WorldBootstrap;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.Map;

class BiomeRepairHelper {
    private Map<ResourceKey<LevelStem>, ChunkGenerator> vanillaDimensions = null;

    public Registry<LevelStem> repairBiomeSourceInAllDimensions(
            RegistryAccess registryAccess,
            Registry<LevelStem> dimensionRegistry
    ) {
        Map<ResourceKey<LevelStem>, ChunkGenerator> dimensions = TogetherWorldPreset.loadWorldDimensions();
        for (var entry : dimensionRegistry.entrySet()) {
            boolean didRepair = false;
            ResourceKey<LevelStem> key = entry.getKey();
            LevelStem loadedStem = entry.getValue();

            ChunkGenerator referenceGenerator = dimensions.get(key);
            if (referenceGenerator instanceof EnforceableChunkGenerator enforcer) {
                final ChunkGenerator loadedChunkGenerator = loadedStem.generator();

                // we ensure that all biomes with a dimensional Tag are properly added to the correct biome source
                // using the correct type
                processBiomeTagsForDimension(key);

                // if the loaded ChunkGenerator is not the one we expect from vanilla, we will load the vanilla
                // ones and mark all modded biomes with the respective dimension
                registerAllBiomesFromVanillaDimension(key);

                // now compare the reference world settings (the ones that were created when the world was
                // started) with the settings that were loaded by the game.
                // If those do not match, we will create a new ChunkGenerator / BiomeSources with appropriate
                // settings
                if (enforcer.togetherShouldRepair(loadedChunkGenerator)) {
                    dimensionRegistry = enforcer.enforceGeneratorInWorldGenSettings(
                            registryAccess,
                            key,
                            loadedStem.type().unwrapKey().orElseThrow(),
                            loadedChunkGenerator,
                            dimensionRegistry
                    );
                    didRepair = true;
                } else if (loadedChunkGenerator.getBiomeSource() instanceof BiomeSourceWithConfig lodedSource) {
                    if (referenceGenerator.getBiomeSource() instanceof BiomeSourceWithConfig refSource) {
                        if (!refSource.getTogetherConfig().sameConfig(lodedSource.getTogetherConfig())) {
                            lodedSource.setTogetherConfig(refSource.getTogetherConfig());
                        }
                    }
                }
            }


            if (!didRepair) {
                if (loadedStem.generator().getBiomeSource() instanceof ReloadableBiomeSource reload) {
                    reload.reloadBiomes();
                }
            }

        }
        return dimensionRegistry;
    }

    private void processBiomeTagsForDimension(ResourceKey<LevelStem> key) {
        if (key.equals(LevelStem.NETHER)) {
            preprocessBiomeTags(BiomeTags.IS_NETHER, BiomeAPI.BiomeType.NETHER);
        } else if (key.equals(LevelStem.END)) {
            preprocessBiomeTags(CommonBiomeTags.IS_END_HIGHLAND, BiomeAPI.BiomeType.END_LAND);
            preprocessBiomeTags(CommonBiomeTags.IS_END_MIDLAND, BiomeAPI.BiomeType.END_LAND);
            preprocessBiomeTags(CommonBiomeTags.IS_END_BARRENS, BiomeAPI.BiomeType.END_BARRENS);
            preprocessBiomeTags(CommonBiomeTags.IS_SMALL_END_ISLAND, BiomeAPI.BiomeType.END_VOID);
            preprocessBiomeTags(CommonBiomeTags.IS_END_CENTER, BiomeAPI.BiomeType.END_CENTER);
            preprocessBiomeTags(BiomeTags.IS_END, BiomeAPI.BiomeType.END_LAND);
        }
    }

    private void preprocessBiomeTags(TagKey<Biome> tag, BiomeAPI.BiomeType targetType) {
        if (WorldBootstrap.getLastRegistryAccess() != null) {
            WorldBootstrap.getLastRegistryAccess()
                          .registry(tag.registry())
                          .map(r -> r.getTagOrEmpty(tag))
                          .ifPresent(iter -> {
                              for (Holder<Biome> biomeHolder : iter) {
                                  BCLBiomeRegistry.registerIfUnknown(biomeHolder, targetType);
                              }
                          });
            ;
        }
    }

    private void registerAllBiomesFromVanillaDimension(
            ResourceKey<LevelStem> key
    ) {
        BiomeAPI.BiomeType type = BiomeAPI.BiomeType.getMainBiomeTypeForDimension(key);

        if (type != null) {
            if (vanillaDimensions == null) {
                vanillaDimensions = TogetherWorldPreset.getDimensionsMap(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);
            }

            final ChunkGenerator vanillaDim = vanillaDimensions.getOrDefault(key, null);
            if (vanillaDim != null && vanillaDim.getBiomeSource() != null) {
                for (Holder<Biome> biomeHolder : vanillaDim.getBiomeSource().possibleBiomes()) {
                    BCLBiomeRegistry.registerIfUnknown(biomeHolder, type);
                }
            }
        }
    }
}
