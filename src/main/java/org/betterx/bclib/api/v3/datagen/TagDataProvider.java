package org.betterx.bclib.api.v3.datagen;

import org.betterx.worlds.together.tag.v3.TagRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;

public class TagDataProvider<T> extends FabricTagProvider<T> {
    @Nullable
    protected final List<String> modIDs;

    protected final TagRegistry<T> tagRegistry;

    private final Set<TagKey<T>> forceWrite;

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
        this(tagRegistry, modIDs, Set.of(), output, registriesFuture);
    }

    /**
     * Constructs a new {@link FabricTagProvider} with the default computed path.
     *
     * <p>Common implementations of this class are provided.
     *
     * @param tagRegistry
     * @param modIDs           List of ModIDs that are allowed to inlcude data. All Resources in the namespace of the
     *                         mod will be written to the tag. If null all elements get written, and empty list will
     *                         write nothing
     * @param forceWriteKeys   the keys that should allways get written
     * @param output           the {@link FabricDataOutput} instance
     * @param registriesFuture the backing registry for the tag type
     */
    public TagDataProvider(
            TagRegistry<T> tagRegistry,
            @Nullable List<String> modIDs,
            Set<TagKey<T>> forceWriteKeys,
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, tagRegistry.registryKey, registriesFuture);
        this.tagRegistry = tagRegistry;
        this.modIDs = modIDs;
        this.forceWrite = forceWriteKeys;
    }

    protected boolean shouldAdd(ResourceLocation loc) {
        return modIDs == null || modIDs.contains(loc.getNamespace());
    }

    protected boolean isOptional(TagEntry e) {
        return (e.verifyIfPresent(id -> false, id -> false));
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        tagRegistry.forEachEntry((tag, locs, tags) -> {
            if (!forceWrite.contains(tag) && locs.isEmpty() && tags.isEmpty()) return;

            final FabricTagProvider<T>.FabricTagBuilder builder = getOrCreateTagBuilder(tag);

            locs.sort(Comparator.comparing(a -> a.first.toString()));
            tags.sort(Comparator.comparing(a -> a.first.location().toString()));

            locs.forEach(pair -> {
                if (isOptional(pair.second)) builder.addOptional(pair.first);
                else builder.add(pair.first);
            });
            
            tags.forEach(pair -> {
                if (isOptional(pair.second)) builder.addOptionalTag(pair.first);
                else builder.forceAddTag(pair.first);
            });
        }, (tag, loc) -> forceWrite.contains(tag) || shouldAdd(tag.location()) || this.shouldAdd(loc));
    }
}
