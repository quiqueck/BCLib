package org.betterx.datagen.bclib;

import org.betterx.bclib.BCLib;
import org.betterx.datagen.bclib.preset.WorldPresetDataProvider;
import org.betterx.datagen.bclib.tests.TestBiomes;
import org.betterx.datagen.bclib.tests.TestConfiguredFeatures;
import org.betterx.datagen.bclib.tests.TestPlacedFeatures;
import org.betterx.datagen.bclib.tests.TestWorldgenProvider;
import org.betterx.datagen.bclib.worldgen.BCLibRegistriesDataProvider;
import org.betterx.datagen.bclib.worldgen.NoiseTypesDataProvider;
import org.betterx.datagen.bclib.worldgen.WorldgenRegistriesDataProvider;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BCLibDatagen implements DataGeneratorEntrypoint {
    public static final boolean ADD_TESTS = true;

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        BCLib.LOGGER.info("Bootstrap onInitializeDataGenerator");
        final FabricDataGenerator.Pack pack = dataGenerator.createPack();

        if (ADD_TESTS) {
            pack.addProvider(TestWorldgenProvider::new);
        }

        pack.addProvider(WorldgenRegistriesDataProvider::new);
        pack.addProvider(WorldPresetDataProvider::new);
        pack.addProvider(BCLibRegistriesDataProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        BCLib.LOGGER.info("Datagen buildRegistry");
        if (ADD_TESTS) {
            registryBuilder.add(Registries.CONFIGURED_FEATURE, TestConfiguredFeatures::bootstrap);
            registryBuilder.add(Registries.PLACED_FEATURE, TestPlacedFeatures::bootstrap);
        }
        registryBuilder.add(Registries.BIOME, TestBiomes::bootstrap);
        registryBuilder.add(Registries.NOISE_SETTINGS, NoiseTypesDataProvider::bootstrap);
        registryBuilder.add(Registries.WORLD_PRESET, WorldPresetDataProvider::bootstrap);
    }
}
