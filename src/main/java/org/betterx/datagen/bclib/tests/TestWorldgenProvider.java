package org.betterx.datagen.bclib.tests;

import net.minecraft.core.HolderLookup;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;

import java.util.concurrent.CompletableFuture;

public class TestWorldgenProvider extends FabricDynamicRegistryProvider {
    public TestWorldgenProvider(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {

    }

    @Override
    public String getName() {
        return "Test WorldGen Provider";
    }
}
