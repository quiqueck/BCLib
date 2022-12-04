package org.betterx.bclib.api.v3.datagen;

import org.betterx.worlds.together.util.Logger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import com.google.gson.JsonElement;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

public abstract class RegistriesDataProvider implements DataProvider {
    public class InfoList extends LinkedList<RegistryInfo<?>> {
        public <T> void add(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
            this.add(new RegistryInfo<T>(key, elementCodec));
        }

        public <T> void add(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, String... modIDs) {
            this.add(new RegistryInfo<T>(key, elementCodec, modIDs));
        }

        public <T> void add(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, List<String> modIDs) {
            this.add(new RegistryInfo<T>(key, elementCodec, modIDs));
        }

        public <T> void addUnfiltered(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
            this.add(new RegistryInfo<T>(key, elementCodec, RegistryInfo.UNFILTERED));
        }
    }

    public final class RegistryInfo<T> {
        public static final List<String> UNFILTERED = null;
        public final RegistryDataLoader.RegistryData<T> data;
        public final List<String> modIDs;

        public RegistryInfo(RegistryDataLoader.RegistryData<T> data, List<String> modIDs) {
            this.data = data;
            this.modIDs = modIDs;
        }

        public RegistryInfo(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
            this(new RegistryDataLoader.RegistryData<>(key, elementCodec), RegistriesDataProvider.this.defaultModIDs);
        }

        public RegistryInfo(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, String... modIDs) {
            this(new RegistryDataLoader.RegistryData<>(key, elementCodec), List.of(modIDs));
        }

        public RegistryInfo(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, List<String> modIDs) {
            this(new RegistryDataLoader.RegistryData<>(key, elementCodec), modIDs);
        }

        public ResourceKey<? extends Registry<T>> key() {
            return data.key();
        }

        public Codec<T> elementCodec() {
            return data.elementCodec();
        }


        List<Holder<T>> allElements(HolderLookup.Provider registryAccess) {
            final HolderLookup.RegistryLookup<T> registry = registryAccess.lookupOrThrow(key());
            return registry
                    .listElementIds()
                    .filter(k -> modIDs == null || modIDs.isEmpty() || modIDs.contains(k.location().getNamespace()))
                    .map(k -> (Holder<T>) registry.get(k).orElseThrow())
                    .toList();
        }

        public RegistryDataLoader.RegistryData<T> data() {
            return data;
        }

        public List<String> modIDs() {
            return modIDs;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (RegistryInfo) obj;
            return Objects.equals(this.data, that.data) &&
                    Objects.equals(this.modIDs, that.modIDs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data, modIDs);
        }

        @Override
        public String toString() {
            return "RegistryInfo[" +
                    "data=" + data + ", " +
                    "modIDs=" + modIDs + ']';
        }

    }

    protected final List<RegistryInfo<?>> registries;

    protected final PackOutput output;
    protected final Logger LOGGER;
    protected final CompletableFuture<HolderLookup.Provider> registriesFuture;
    private @Nullable List<String> defaultModIDs;

    protected RegistriesDataProvider(
            Logger logger,
            @Nullable List<String> defaultModIDs,
            FabricDataOutput generator,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        this.defaultModIDs = defaultModIDs;
        this.LOGGER = logger;
        this.output = generator;
        this.registriesFuture = registriesFuture;
        this.registries = initializeRegistryList(defaultModIDs);
    }

    protected abstract List<RegistryInfo<?>> initializeRegistryList(@Nullable List<String> modIDs);

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        LOGGER.info("Serialize Registries " + defaultModIDs);

        return registriesFuture.thenCompose(registriesProvider -> CompletableFuture
                .supplyAsync(() -> registries)
                .thenCompose(entries -> {
                    final List<CompletableFuture<?>> futures = new ArrayList<>();
                    final RegistryOps<JsonElement> dynamicOps = RegistryOps.create(
                            JsonOps.INSTANCE,
                            registriesProvider
                    );

                    futures.add(CompletableFuture.runAsync(() -> {
                        for (RegistryInfo<?> registryData : entries) {
//                        futures.add(CompletableFuture.runAsync(() ->
                            serializeRegistry(
                                    cachedOutput, registriesProvider, dynamicOps, registryData
                            );
//                        ));
                        }
                    }));

                    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
                }));
    }

    private <T> void serializeRegistry(
            CachedOutput cachedOutput,
            HolderLookup.Provider registryAccess,
            DynamicOps<JsonElement> dynamicOps,
            RegistryInfo<T> registryData
    ) {
        final List<Holder<T>> elements = registryData.allElements(registryAccess);

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

    @Override
    public String getName() {
        return "Registries";
    }
}