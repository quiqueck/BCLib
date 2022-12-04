package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeData;
import org.betterx.bclib.api.v3.datagen.RegistriesDataProvider;
import org.betterx.datagen.bclib.BCLibDatagen;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.surfaceRules.AssignedSurfaceRule;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

public class BCLibRegistriesDataProvider extends RegistriesDataProvider {
    public BCLibRegistriesDataProvider(
            FabricDataOutput generator,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(BCLib.LOGGER, List.of(BCLib.MOD_ID, WorldsTogether.MOD_ID), generator, registriesFuture);
    }


    @Override
    protected List<RegistryInfo<?>> initializeRegistryList(@Nullable List<String> modIDs) {
        InfoList registries = new InfoList();

        registries.addUnfiltered(BCLBiomeRegistry.BCL_BIOMES_REGISTRY, BiomeData.CODEC);
        registries.addUnfiltered(SurfaceRuleRegistry.SURFACE_RULES_REGISTRY, AssignedSurfaceRule.CODEC);

        if (BCLibDatagen.ADD_TESTS) {
            registries.add(Registries.STRUCTURE, Structure.DIRECT_CODEC);
            registries.add(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC);
            registries.add(Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC);
            registries.add(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC);
            registries.add(Registries.BIOME, Biome.DIRECT_CODEC);
        }

        registries.add(Registries.NOISE_SETTINGS, NoiseGeneratorSettings.DIRECT_CODEC);
        registries.add(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC);

        return registries;
    }
}
