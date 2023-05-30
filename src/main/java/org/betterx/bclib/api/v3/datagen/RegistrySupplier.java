package org.betterx.bclib.api.v3.datagen;

import org.betterx.bclib.BCLib;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import org.jetbrains.annotations.Nullable;

public abstract class RegistrySupplier {
    private static final int MAX_PERMITS = 2000;
    private final Semaphore BOOTSTRAP_LOCK = new Semaphore(MAX_PERMITS);
    public final Semaphore MAIN_LOCK = new Semaphore(1);

    final List<RegistrySupplier.RegistryInfo<?>> allRegistries;
    @Nullable List<String> defaultModIDs;

    protected RegistrySupplier(
            @Nullable List<String> defaultModIDs
    ) {
        this.defaultModIDs = defaultModIDs;
        this.allRegistries = initializeRegistryList(defaultModIDs);
        try {
            BOOTSTRAP_LOCK.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract List<RegistrySupplier.RegistryInfo<?>> initializeRegistryList(@Nullable List<String> modIDs);

    public void bootstrapRegistries(RegistrySetBuilder registryBuilder) {
        for (RegistrySupplier.RegistryInfo<?> nfo : allRegistries) {
            nfo.add(registryBuilder, BOOTSTRAP_LOCK);
        }
        BOOTSTRAP_LOCK.release();
    }

    void acquireLock() {
        try {
            BOOTSTRAP_LOCK.acquire(MAX_PERMITS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void releaseLock() {
        BOOTSTRAP_LOCK.release(MAX_PERMITS);
    }

    public class InfoList extends LinkedList<RegistrySupplier.RegistryInfo<?>> {
        public <T> void add(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
            this.add(new RegistrySupplier.RegistryInfo<T>(key, elementCodec));
        }

        public <T> void add(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, String... modIDs) {
            this.add(new RegistrySupplier.RegistryInfo<T>(key, elementCodec, modIDs));
        }

        public <T> void add(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, List<String> modIDs) {
            this.add(new RegistrySupplier.RegistryInfo<T>(key, elementCodec, modIDs));
        }

        public <T> void addUnfiltered(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
            this.add(new RegistrySupplier.RegistryInfo<T>(
                    key,
                    elementCodec,
                    RegistrySupplier.RegistryInfo.UNFILTERED
            ));
        }

        public <T> void add(
                ResourceKey<? extends Registry<T>> key,
                Codec<T> elementCodec,
                RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap
        ) {
            this.add(new RegistrySupplier.RegistryInfo<T>(key, elementCodec, registryBootstrap));
        }

        public <T> void add(
                ResourceKey<? extends Registry<T>> key,
                Codec<T> elementCodec,
                RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap,
                String... modIDs
        ) {
            this.add(new RegistrySupplier.RegistryInfo<T>(key, elementCodec, registryBootstrap, modIDs));
        }

        public <T> void add(
                ResourceKey<? extends Registry<T>> key,
                Codec<T> elementCodec,
                RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap,
                List<String> modIDs
        ) {
            this.add(new RegistrySupplier.RegistryInfo<T>(key, elementCodec, registryBootstrap, modIDs));
        }

        public <T> void addUnfiltered(
                ResourceKey<? extends Registry<T>> key,
                Codec<T> elementCodec,
                RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap
        ) {
            this.add(new RegistrySupplier.RegistryInfo<T>(
                    key,
                    elementCodec,
                    registryBootstrap,
                    RegistryInfo.UNFILTERED
            ));
        }

        public <T> void addBootstrapOnly(
                ResourceKey<? extends Registry<T>> key,
                Codec<T> elementCodec,
                RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap
        ) {
            this.add(new RegistrySupplier.RegistryInfo<T>(key, elementCodec, registryBootstrap, List.of()));
        }
    }

    public final class RegistryInfo<T> {
        public static final List<String> UNFILTERED = null;
        public final RegistryDataLoader.RegistryData<T> data;
        public final List<String> modIDs;
        public final RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap;

        public RegistryInfo(
                RegistryDataLoader.RegistryData<T> data,
                List<String> modIDs,
                RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap
        ) {
            this.data = data;
            this.modIDs = modIDs;
            this.registryBootstrap = registryBootstrap;
        }

        public RegistryInfo(
                ResourceKey<? extends Registry<T>> key,
                Codec<T> elementCodec,
                RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap
        ) {
            this(
                    new RegistryDataLoader.RegistryData<>(key, elementCodec),
                    RegistrySupplier.this.defaultModIDs,
                    registryBootstrap
            );
        }

        public RegistryInfo(
                ResourceKey<? extends Registry<T>> key,
                Codec<T> elementCodec,
                RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap,
                String... modIDs
        ) {
            this(new RegistryDataLoader.RegistryData<>(key, elementCodec), List.of(modIDs), registryBootstrap);
        }

        public RegistryInfo(
                ResourceKey<? extends Registry<T>> key,
                Codec<T> elementCodec,
                RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap,
                List<String> modIDs
        ) {
            this(new RegistryDataLoader.RegistryData<>(key, elementCodec), modIDs, registryBootstrap);
        }

        public RegistryInfo(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
            this(
                    new RegistryDataLoader.RegistryData<>(key, elementCodec),
                    RegistrySupplier.this.defaultModIDs,
                    null
            );
        }

        public RegistryInfo(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, String... modIDs) {
            this(new RegistryDataLoader.RegistryData<>(key, elementCodec), List.of(modIDs), null);
        }

        public RegistryInfo(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, List<String> modIDs) {
            this(new RegistryDataLoader.RegistryData<>(key, elementCodec), modIDs, null);
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
                    .filter(k -> modIDs == null || modIDs.contains(k.location().getNamespace()))
                    .map(k -> (Holder<T>) registry.get(k).orElseThrow())
                    .toList();
        }


        private void add(RegistrySetBuilder registryBuilder, final Semaphore LOCK_BOOSTRAP) {
            try {
                LOCK_BOOSTRAP.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            BCLib.LOGGER.info("Adding:" + key());
            registryBuilder.add(key(), (BootstapContext<T> ctx) -> {
                if (registryBootstrap != null) {
                    registryBootstrap.run(ctx);
                }
                LOCK_BOOSTRAP.release();
            });
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
}
