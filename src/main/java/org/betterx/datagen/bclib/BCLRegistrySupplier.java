package org.betterx.datagen.bclib;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeData;
import org.betterx.bclib.api.v3.datagen.RegistrySupplier;
import org.betterx.datagen.bclib.preset.WorldPresetDataProvider;
import org.betterx.datagen.bclib.tests.TestBiomes;
import org.betterx.datagen.bclib.tests.TestConfiguredFeatures;
import org.betterx.datagen.bclib.tests.TestPlacedFeatures;
import org.betterx.datagen.bclib.tests.TestStructure;
import org.betterx.datagen.bclib.worldgen.BiomeDatagenProvider;
import org.betterx.datagen.bclib.worldgen.NoiseTypesDataProvider;
import org.betterx.datagen.bclib.worldgen.VanillaBCLBiomesDataProvider;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.surfaceRules.AssignedSurfaceRule;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class BCLRegistrySupplier extends RegistrySupplier {
    public static final BCLRegistrySupplier INSTANCE = new BCLRegistrySupplier();

    private BCLRegistrySupplier() {
        super(List.of(
                BCLib.MOD_ID,
                WorldsTogether.MOD_ID
        ));
    }

    @Override
    protected List<RegistryInfo<?>> initializeRegistryList(@Nullable List<String> modIDs) {
        InfoList registries = new InfoList();

        registries.addUnfiltered(
                BCLBiomeRegistry.BCL_BIOMES_REGISTRY,
                BiomeData.CODEC,
                VanillaBCLBiomesDataProvider::bootstrap
        );
        registries.addUnfiltered(SurfaceRuleRegistry.SURFACE_RULES_REGISTRY, AssignedSurfaceRule.CODEC);

        if (BCLib.ADD_TEST_DATA) {
            registries.add(Registries.STRUCTURE, Structure.DIRECT_CODEC, TestStructure::bootstrap);
            registries.add(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC, TestStructure::bootstrapSet);
            registries.add(
                    Registries.CONFIGURED_FEATURE,
                    ConfiguredFeature.DIRECT_CODEC,
                    TestConfiguredFeatures::bootstrap
            );
            registries.add(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC, TestPlacedFeatures::bootstrap);
            registries.add(Registries.BIOME, Biome.DIRECT_CODEC, TestBiomes::bootstrap);
        } else {
            registries.add(Registries.BIOME, Biome.DIRECT_CODEC, BiomeDatagenProvider::bootstrap);
        }

        registries.add(
                Registries.NOISE_SETTINGS,
                NoiseGeneratorSettings.DIRECT_CODEC,
                NoiseTypesDataProvider::bootstrap
        );
        registries.add(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC, WorldPresetDataProvider::bootstrap);

        return registries;
    }
}
