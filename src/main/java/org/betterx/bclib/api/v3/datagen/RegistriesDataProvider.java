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
                            registries.acquireLock();
                            final RegistryOps<JsonElement> dynamicOps = RegistryOps.create(
                                    JsonOps.INSTANCE,
                                    registriesProvider
                            );

                            CompletableFuture<?>[] futures = entries
                                    .allRegistries
                                    .stream()
                                    .map(registryData -> serializeRegistry(
                                            cachedOutput,
                                            registriesProvider,
                                            dynamicOps,
                                            registryData,
                                            registries
                                    ))
                                    .toArray(CompletableFuture<?>[]::new);

                            registries.releaseLock();
                            return CompletableFuture.allOf(futures);
                        }
                )
        );
    }


    private <T> CompletableFuture<?> serializeRegistry(
            CachedOutput cachedOutput,
            HolderLookup.Provider registryAccess,
            DynamicOps<JsonElement> dynamicOps,
            RegistrySupplier.RegistryInfo<T> registryData,
            RegistrySupplier registries
    ) {
        try {
            registries.MAIN_LOCK.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        final List<Holder<T>> elements = registryData.allElements(registryAccess);
        final HolderLookup.RegistryLookup<T> registry = registryAccess.lookupOrThrow(registryData.key());
        LOGGER.info("Serializing "
                + elements.size()
                + "/"
                + registry.listElements().count()
                + " elements from "
                + registryData.data.key()
        );
        registries.MAIN_LOCK.release();

        if (!elements.isEmpty()) {
            PackOutput.PathProvider pathProvider = this.output.createPathProvider(
                    PackOutput.Target.DATA_PACK,
                    registryData.key().location().getPath()
            );

            return CompletableFuture.allOf(elements.stream().map(entry -> serializeElements(
                    pathProvider.json(entry.unwrapKey().orElseThrow().location()),
                    cachedOutput,
                    dynamicOps,
                    registryData.elementCodec(),
                    entry.value()
            )).toArray(CompletableFuture[]::new));
        }
        return CompletableFuture.completedFuture(null);
    }

    private <E> CompletableFuture<?> serializeElements(
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
            return DataProvider.saveStable(cachedOutput, optional.get(), path);
        }
        return CompletableFuture.completedFuture(null);
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