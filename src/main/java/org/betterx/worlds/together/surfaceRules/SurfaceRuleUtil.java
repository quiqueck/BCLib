package org.betterx.worlds.together.surfaceRules;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.chunkgenerator.InjectableSurfaceRules;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SurfaceRuleUtil {
    private static List<SurfaceRules.RuleSource> getRulesForBiome(ResourceLocation biomeID) {
        Registry<AssignedSurfaceRule> registry = null;
        if (WorldBootstrap.getLastRegistryAccess() != null)
            registry = WorldBootstrap.getLastRegistryAccess()
                                     .registryOrThrow(SurfaceRuleRegistry.SURFACE_RULES_REGISTRY);

        if (registry == null) return List.of();

        return registry.stream()
                       .filter(a -> a != null && a.biomeID != null && a.biomeID.equals(
                               biomeID))
                       .map(a -> a.ruleSource)
                       .toList();

    }

    private static List<SurfaceRules.RuleSource> getRulesForBiomes(List<Biome> biomes) {
        Registry<Biome> biomeRegistry = WorldBootstrap.getLastRegistryAccess().registryOrThrow(Registries.BIOME);
        List<ResourceLocation> biomeIDs = biomes.stream()
                                                .map(b -> biomeRegistry.getKey(b))
                                                .filter(id -> id != null)
                                                .toList();

        return biomeIDs.stream()
                       .map(biomeID -> getRulesForBiome(biomeID))
                       .flatMap(List::stream)
                       .collect(Collectors.toCollection(LinkedList::new));
    }

    private static SurfaceRules.RuleSource mergeSurfaceRules(
            ResourceKey<LevelStem> dimensionKey,
            SurfaceRules.RuleSource org,
            BiomeSource source,
            List<SurfaceRules.RuleSource> additionalRules
    ) {
        if (additionalRules == null || additionalRules.isEmpty()) return null;
        final int count = additionalRules.size();
        if (org instanceof SurfaceRules.SequenceRuleSource sequenceRule) {
            List<SurfaceRules.RuleSource> existingSequence = sequenceRule.sequence();
            additionalRules = additionalRules
                    .stream()
                    .filter(r -> existingSequence.indexOf(r) < 0)
                    .collect(Collectors.toList());
            if (additionalRules.isEmpty()) return null;

            // when we are in the nether, we want to keep the nether roof and floor rules in the beginning of the sequence
            // we will add our rules whne the first biome test sequence is found
            if (dimensionKey.equals(LevelStem.NETHER)) {
                final List<SurfaceRules.RuleSource> combined = new ArrayList<>(existingSequence.size() + additionalRules.size());
                for (SurfaceRules.RuleSource rule : existingSequence) {
                    if (rule instanceof SurfaceRules.TestRuleSource testRule
                            && testRule.ifTrue() instanceof SurfaceRules.BiomeConditionSource) {
                        combined.addAll(additionalRules);
                    }
                    combined.add(rule);
                }
                additionalRules = combined;
            } else {
                additionalRules.addAll(existingSequence);
            }
        } else {
            if (!additionalRules.contains(org))
                additionalRules.add(org);
        }

        if (Configs.MAIN_CONFIG.verboseLogging()) {
            BCLib.LOGGER.info("Merged " + count + " additional Surface Rules for " + source + " => " + additionalRules.size());
        }
        return new SurfaceRules.SequenceRuleSource(additionalRules);
    }

    public static void injectSurfaceRules(
            ResourceKey<LevelStem> dimensionKey,
            NoiseGeneratorSettings noiseSettings,
            BiomeSource loadedBiomeSource
    ) {
        if (((Object) noiseSettings) instanceof SurfaceRuleProvider srp) {
            SurfaceRules.RuleSource originalRules = srp.bclib_getOriginalSurfaceRules();
            srp.bclib_overwriteSurfaceRules(mergeSurfaceRules(
                    dimensionKey,
                    originalRules,
                    loadedBiomeSource,
                    getRulesForBiomes(loadedBiomeSource.possibleBiomes().stream().map(h -> h.value()).toList())
            ));
        }
    }

    public static void injectSurfaceRulesToAllDimensions(Registry<LevelStem> dimensionRegistry) {
        for (var entry : dimensionRegistry.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            LevelStem stem = entry.getValue();

            if (stem.generator() instanceof InjectableSurfaceRules<?> generator) {
                generator.injectSurfaceRules(key);
            }
        }
    }
}
