package org.betterx.worlds.together.surfaceRules;

import org.betterx.worlds.together.chunkgenerator.InjectableSurfaceRules;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SurfaceRuleUtil {
    private static List<SurfaceRules.RuleSource> getRulesForBiome(ResourceLocation biomeID) {
        Registry<AssignedSurfaceRule> registry = SurfaceRuleRegistry.BUILTIN_SURFACE_RULES;
        if (WorldBootstrap.getLastRegistryAccess() != null)
            registry = WorldBootstrap.getLastRegistryAccess()
                                     .registryOrThrow(SurfaceRuleRegistry.SURFACE_RULES_REGISTRY);
        return registry.stream()
                       .filter(a -> a != null && a.biomeID != null && a.biomeID.equals(
                               biomeID))
                       .map(a -> a.ruleSource)
                       .toList();

    }

    private static List<SurfaceRules.RuleSource> getRulesForBiomes(List<Biome> biomes) {
        Registry<Biome> biomeRegistry = WorldBootstrap.getLastRegistryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        List<ResourceLocation> biomeIDs = biomes.stream()
                                                .map(b -> biomeRegistry.getKey(b))
                                                .filter(id -> id != null)
                                                .toList();

        return biomeIDs.stream()
                       .map(biomeID -> getRulesForBiome(biomeID))
                       .flatMap(List::stream)
                       .collect(Collectors.toCollection(LinkedList::new));
    }

    private static SurfaceRules.RuleSource mergeSurfaceRulesFromBiomes(
            SurfaceRules.RuleSource org,
            BiomeSource source
    ) {
        return mergeSurfaceRules(
                org,
                getRulesForBiomes(source.possibleBiomes().stream().map(h -> h.value()).toList())
        );
    }

    private static SurfaceRules.RuleSource mergeSurfaceRules(
            SurfaceRules.RuleSource org,
            List<SurfaceRules.RuleSource> additionalRules
    ) {
        if (additionalRules == null || additionalRules.isEmpty()) return org;

        if (org instanceof SurfaceRules.SequenceRuleSource sequenceRule) {
            List<SurfaceRules.RuleSource> existingSequence = sequenceRule.sequence();
            additionalRules = additionalRules
                    .stream()
                    .filter(r -> existingSequence.indexOf(r) < 0)
                    .collect(Collectors.toList());
            if (additionalRules.size() == 0) return org;
            additionalRules.addAll(existingSequence);
        } else {
            if (!additionalRules.contains(org))
                additionalRules.add(org);
        }

        return new SurfaceRules.SequenceRuleSource(additionalRules);
    }

    public static void injectSurfaceRules(NoiseGeneratorSettings noiseSettings, BiomeSource loadedBiomeSource) {
        if (((Object) noiseSettings) instanceof SurfaceRuleProvider srp) {
            SurfaceRules.RuleSource originalRules = noiseSettings.surfaceRule();
            srp.bclib_overwrite(mergeSurfaceRulesFromBiomes(originalRules, loadedBiomeSource));
        }
    }

    public static void injectSurfaceRulesToAllDimensions(WorldGenSettings settings) {
        for (var entry : settings.dimensions().dimensions().entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            LevelStem stem = entry.getValue();

            if (stem.generator() instanceof InjectableSurfaceRules<?> generator) {
                generator.injectSurfaceRules(key);
            }
        }
    }
}
