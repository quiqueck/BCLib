package org.betterx.worlds.together;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.worlds.together.surfaceRules.AssignedSurfaceRule;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBuiltinRegistriesProvider;

import com.google.gson.JsonElement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WorldsTogetherDatagen implements DataGeneratorEntrypoint {


    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(FabricBuiltinRegistriesProvider.forCurrentMod()
                                                                       .apply(fabricDataGenerator));
        fabricDataGenerator.addProvider(CustomRegistriesDataProvider::new);
    }

    public static class CustomRegistriesDataProvider implements DataProvider {
        public static final List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
                new RegistryDataLoader.RegistryData<>(BCLBiomeRegistry.BCL_BIOMES_REGISTRY, BCLBiome.CODEC),
                new RegistryDataLoader.RegistryData<>(
                        SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
                        AssignedSurfaceRule.CODEC
                )
        );


        private final PackOutput output;

        public CustomRegistriesDataProvider(FabricDataGenerator generator) {
            this.output = generator.getVanillaPackOutput();
        }

        @Override
        public void run(CachedOutput cachedOutput) {
            RegistryAccess.Frozen registryAccess = BuiltinRegistries.createAccess();
            RegistryOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            REGISTRIES.forEach(registryData -> this.dumpRegistryCap(
                    cachedOutput,
                    registryAccess,
                    dynamicOps,
                    (RegistryDataLoader.RegistryData) registryData
            ));
        }

        private <T> void dumpRegistryCap(
                CachedOutput cachedOutput,
                RegistryAccess registryAccess,
                DynamicOps<JsonElement> dynamicOps,
                RegistryDataLoader.RegistryData<T> registryData
        ) {
            ResourceKey<? extends Registry<T>> resourceKey = registryData.key();
            Registry<T> registry = registryAccess.registryOrThrow(resourceKey);
            PackOutput.PathProvider pathProvider = this.output.createPathProvider(
                    PackOutput.Target.DATA_PACK,
                    resourceKey.location().getPath()
            );
            for (Map.Entry<ResourceKey<T>, T> entry : registry.entrySet()) {
                dumpValue(
                        pathProvider.json(entry.getKey().location()),
                        cachedOutput,
                        dynamicOps,
                        registryData.elementCodec(),
                        entry.getValue()
                );
            }
        }

        private static <E> void dumpValue(
                Path path,
                CachedOutput cachedOutput,
                DynamicOps<JsonElement> dynamicOps,
                Encoder<E> encoder,
                E object
        ) {
            try {
                Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
                                                        .resultOrPartial(string -> WorldsTogether.LOGGER.error(
                                                                "Couldn't serialize element {}: {}",
                                                                path,
                                                                string
                                                        ));
                if (optional.isPresent()) {
                    DataProvider.saveStable(cachedOutput, optional.get(), path);
                }
            } catch (IOException iOException) {
                WorldsTogether.LOGGER.error("Couldn't save element {}", path, iOException);
            }
        }

        @Override
        public String getName() {
            return "BCL Registries";
        }
    }
}