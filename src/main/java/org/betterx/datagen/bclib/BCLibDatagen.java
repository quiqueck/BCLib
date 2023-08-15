package org.betterx.datagen.bclib;

import org.betterx.bclib.BCLib;
import org.betterx.datagen.bclib.advancement.BCLAdvancementDataProvider;
import org.betterx.datagen.bclib.advancement.RecipeDataProvider;
import org.betterx.datagen.bclib.integrations.NullscapeBiomes;
import org.betterx.datagen.bclib.preset.WorldPresetDataProvider;
import org.betterx.datagen.bclib.tests.TestBiomes;
import org.betterx.datagen.bclib.tests.TestWorldgenProvider;
import org.betterx.datagen.bclib.worldgen.BCLibRegistriesDataProvider;
import org.betterx.datagen.bclib.worldgen.BiomeDatagenProvider;
import org.betterx.datagen.bclib.worldgen.BlockTagProvider;
import org.betterx.datagen.bclib.worldgen.ItemTagProvider;

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

            pack.addProvider(TestWorldgenProvider::new);
            pack.addProvider(TestBiomes::new);
            RecipeDataProvider.createTestRecipes();
        } else {
            pack.addProvider(BiomeDatagenProvider::new);
        }

        pack.addProvider(BlockTagProvider::new);
        pack.addProvider(ItemTagProvider::new);
        pack.addProvider(RecipeDataProvider::new);
        pack.addProvider(WorldPresetDataProvider::new);
        pack.addProvider(BCLibRegistriesDataProvider::new);
        pack.addProvider(BCLAdvancementDataProvider::new);
    }


    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        BCLRegistrySupplier.INSTANCE.bootstrapRegistries(registryBuilder);
    }
}
