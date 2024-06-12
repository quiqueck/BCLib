package org.betterx.datagen.bclib;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeData;
import org.betterx.bclib.api.v3.datagen.RegistrySupplier;
import org.betterx.datagen.bclib.worldgen.BiomeDatagenProvider;
import org.betterx.datagen.bclib.worldgen.VanillaBCLBiomesDataProvider;
import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;

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


        registries.add(Registries.BIOME, Biome.DIRECT_CODEC, BiomeDatagenProvider::bootstrap);


        return registries;
    }
}
