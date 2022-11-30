package org.betterx.bclib.datagen;

import org.betterx.bclib.BCLib;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BCLibDatagen implements DataGeneratorEntrypoint {
    static boolean ADD_TESTS = true;

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        BCLib.LOGGER.info("Bootstrap onInitializeDataGenerator");
        final FabricDataGenerator.Pack pack = dataGenerator.createPack();

        if (ADD_TESTS) {
            pack.addProvider(TestWorldgenProvider::new);
        }
        pack.addProvider(WorldgenProvider::new);
        //pack.addProvider(BiomeProvider::new);
        //pack.addProvider(new BiomeProvider());
        pack.addProvider(CustomRegistriesDataProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        BCLib.LOGGER.info("Datagen buildRegistry");
        if (ADD_TESTS) {
            registryBuilder.add(Registries.CONFIGURED_FEATURE, TestConfiguredFeatures::bootstrap);
            registryBuilder.add(Registries.PLACED_FEATURE, TestPlacedFeatures::bootstrap);
        }
        registryBuilder.add(Registries.BIOME, TestBiomes::bootstrap);
        registryBuilder.add(Registries.NOISE_SETTINGS, NoiseDatagen::bootstrap);
    }
}
