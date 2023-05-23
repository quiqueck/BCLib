package org.betterx.datagen.bclib;

import org.betterx.bclib.BCLib;
import org.betterx.datagen.bclib.advancement.BCLAdvancementDataProvider;
import org.betterx.datagen.bclib.advancement.RecipeDataProvider;
import org.betterx.datagen.bclib.integrations.NullscapeBiomes;
import org.betterx.datagen.bclib.preset.WorldPresetDataProvider;
import org.betterx.datagen.bclib.tests.TestBiomes;
import org.betterx.datagen.bclib.tests.TestWorldgenProvider;
import org.betterx.datagen.bclib.worldgen.BCLibRegistriesDataProvider;

import net.minecraft.core.RegistrySetBuilder;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BCLibDatagen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        BCLib.LOGGER.info("Bootstrap onInitializeDataGenerator");
        final FabricDataGenerator.Pack pack = dataGenerator.createPack();

        NullscapeBiomes.ensureStaticallyLoaded();
        if (BCLib.ADD_TEST_DATA) {
            TestBiomes.ensureStaticallyLoaded();

            BCLRegistrySupplier.INSTANCE.addProviderWithLock(pack, TestWorldgenProvider::new);
            BCLRegistrySupplier.INSTANCE.addProviderWithLock(pack, TestBiomes::new);
            RecipeDataProvider.createTestRecipes();
        } else {
            BCLRegistrySupplier.INSTANCE.addProviderWithLock(pack, NullscapeBiomes::new);
        }

        BCLRegistrySupplier.INSTANCE.addProviderWithLock(pack, RecipeDataProvider::new);
        BCLRegistrySupplier.INSTANCE.addProviderWithLock(pack, WorldPresetDataProvider::new);
        BCLRegistrySupplier.INSTANCE.addProviderWithLock(pack, BCLibRegistriesDataProvider::new);
        BCLRegistrySupplier.INSTANCE.addProviderWithLock(pack, BCLAdvancementDataProvider::new);
    }


    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        BCLRegistrySupplier.INSTANCE.bootstrapRegistries(registryBuilder);
    }
}
