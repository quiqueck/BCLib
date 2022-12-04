package org.betterx.bclib.api.v3.datagen;

import org.betterx.worlds.together.util.Logger;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import com.google.gson.JsonElement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class RegistriesDataProvider implements DataProvider {


    protected final RegistrySupplier registries;
    protected final PackOutput output;
    protected final Logger LOGGER;
    protected final CompletableFuture<HolderLookup.Provider> registriesFuture;

    protected RegistriesDataProvider(
            Logger logger,
            RegistrySupplier registries,
            FabricDataOutput generator,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        this.LOGGER = logger;
        this.output = generator;
        this.registriesFuture = registriesFuture;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        LOGGER.info("Serialize Registries " + registries.defaultModIDs);

        return registriesFuture.thenCompose(registriesProvider -> CompletableFuture
                .supplyAsync(() -> registries)
                .thenCompose(entries -> {
                    final List<CompletableFuture<?>> futures = new ArrayList<>();

                    futures.add(CompletableFuture.runAsync(() -> {
                        registries.acquireLock();
                        final RegistryOps<JsonElement> dynamicOps = RegistryOps.create(
                                JsonOps.INSTANCE,
                                registriesProvider
                        );

                        for (RegistrySupplier.RegistryInfo<?> registryData : entries.allRegistries) {
                            serializeRegistry(
                                    cachedOutput, registriesProvider, dynamicOps, registryData
                            );
                        }
                        registries.releaseLock();
                    }));

                    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
                }));
    }

    private <T> void serializeRegistry(
            CachedOutput cachedOutput,
            HolderLookup.Provider registryAccess,
            DynamicOps<JsonElement> dynamicOps,
            RegistrySupplier.RegistryInfo<T> registryData
    ) {
        final List<Holder<T>> elements = registryData.allElements(registryAccess);
        LOGGER.info("Serializing " + elements.size() + " elements from " + registryData.data.key());

        if (!elements.isEmpty()) {
            PackOutput.PathProvider pathProvider = this.output.createPathProvider(
                    PackOutput.Target.DATA_PACK,
                    registryData.key().location().getPath()
            );

            elements.forEach(entry ->
                    serializeElements(
                            pathProvider.json(entry.unwrapKey().orElseThrow().location()),
                            cachedOutput,
                            dynamicOps,
                            registryData.elementCodec(),
                            entry.value()
                    )
            );
        }
    }

    private <E> void serializeElements(
            Path path,
            CachedOutput cachedOutput,
            DynamicOps<JsonElement> dynamicOps,
            Encoder<E> encoder,
            E object
    ) {
        Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
                                                .resultOrPartial(string -> this.LOGGER.error(
                                                        "Couldn't serialize element {}: {}",
                                                        path,
                                                        string
                                                ));
        if (optional.isPresent()) {
            DataProvider.saveStable(cachedOutput, optional.get(), path);
        }
    }

    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registries.allRegistries
                .stream()
                .filter(nfo -> nfo.registryBootstrap != null)
                .forEach(nfo -> {

                });
    }

    @Override
    public String getName() {
        return "Registries";
    }
}