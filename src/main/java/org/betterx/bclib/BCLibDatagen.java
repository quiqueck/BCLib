package org.betterx.bclib;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeData;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.surfaceRules.AssignedSurfaceRule;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleRegistry;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBuiltinRegistriesProvider;

import com.google.gson.JsonElement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BCLibDatagen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        final FabricDataGenerator.Pack pack = dataGenerator.createPack();
        pack.addProvider(FabricBuiltinRegistriesProvider.forCurrentMod());
        pack.addProvider(CustomRegistriesDataProvider::new);
    }

    public static class CustomRegistriesDataProvider implements DataProvider {
        public static final List<RegistryDataLoader.RegistryData<?>> REGISTRIES = List.of(
                new RegistryDataLoader.RegistryData<>(BCLBiomeRegistry.BCL_BIOMES_REGISTRY, BiomeData.CODEC),
                new RegistryDataLoader.RegistryData<>(
                        SurfaceRuleRegistry.SURFACE_RULES_REGISTRY,
                        AssignedSurfaceRule.CODEC
                )
        );


        private final PackOutput output;

        public CustomRegistriesDataProvider(FabricDataOutput generator) {
            this.output = generator;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cachedOutput) {
            RegistryAccess.Frozen registryAccess = BuiltInRegistries.createAccess();
            RegistryOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            final List<CompletableFuture<?>> futures = new ArrayList<>();

            for (RegistryDataLoader.RegistryData<?> registryData : REGISTRIES) {
                futures.add(this.dumpRegistryCapFuture(
                        cachedOutput,
                        registryAccess,
                        dynamicOps,
                        (RegistryDataLoader.RegistryData) registryData
                ));
            }
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        }

        private <T> CompletableFuture dumpRegistryCapFuture(
                CachedOutput cachedOutput,
                RegistryAccess registryAccess,
                DynamicOps<JsonElement> dynamicOps,
                RegistryDataLoader.RegistryData<T> registryData
        ) {
            return CompletableFuture.runAsync(() -> dumpRegistryCap(
                    cachedOutput,
                    registryAccess,
                    dynamicOps,
                    registryData
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

            Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
                                                    .resultOrPartial(string -> WorldsTogether.LOGGER.error(
                                                            "Couldn't serialize element {}: {}",
                                                            path,
                                                            string
                                                    ));
            if (optional.isPresent()) {
                DataProvider.saveStable(cachedOutput, optional.get(), path);
            }

        }

        @Override
        public String getName() {
            return "BCL Registries";
        }
    }
}
