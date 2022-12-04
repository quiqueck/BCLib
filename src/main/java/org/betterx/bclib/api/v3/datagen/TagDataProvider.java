package org.betterx.bclib.api.v3.datagen;

import org.betterx.worlds.together.tag.v3.TagRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

public class TagDataProvider<T> extends FabricTagProvider<T> {
    @Nullable
    protected final List<String> modIDs;

    protected final TagRegistry<T> tagRegistry;

    /**
     * Constructs a new {@link FabricTagProvider} with the default computed path.
     *
     * <p>Common implementations of this class are provided.
     *
     * @param tagRegistry
     * @param modIDs           List of ModIDs that are allowed to inlcude data. All Resources in the namespace of the
     *                         mod will be written to the tag. If null all elements get written, and empty list will
     *                         write nothing
     * @param output           the {@link FabricDataOutput} instance
     * @param registriesFuture the backing registry for the tag type
     */
    public TagDataProvider(
            TagRegistry<T> tagRegistry,
            @Nullable List<String> modIDs,
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, tagRegistry.registryKey, registriesFuture);
        this.tagRegistry = tagRegistry;
        this.modIDs = modIDs;
    }

    protected boolean shouldAdd(ResourceLocation loc) {
        return modIDs == null || modIDs.contains(loc.getNamespace());
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        tagRegistry.forEachTag((tag, locs, tags) -> {
            final FabricTagProvider<T>.FabricTagBuilder builder = getOrCreateTagBuilder(tag);

            boolean modTag = shouldAdd(tag.location());
            locs.stream()
                .filter(loc -> modTag || this.shouldAdd(loc))
                .forEach(builder::add);

            tags.stream()
                .filter(tagKey -> modTag || this.shouldAdd(tagKey.location()))
                .forEach(builder::addTag);
        });
    }
}
