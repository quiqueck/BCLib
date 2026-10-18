package org.betterx.bclib.api.v3.datagen;

import org.betterx.bclib.util.Pair;
import org.betterx.worlds.together.tag.v3.TagRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        final Map<TagKey<T>, List<Pair<ResourceLocation, TagEntry>>> elementsByTag = new HashMap<>();
        final Map<TagKey<T>, List<Pair<TagKey<T>, TagEntry>>> tagsByTag = new HashMap<>();

        tagRegistry.forEachEntry((tag, locs, tags) -> {
            if (!forceWrite.contains(tag) && locs.isEmpty() && tags.isEmpty()) return;
            elementsByTag.put(tag, locs);
            tagsByTag.put(tag, tags);
        }, (tag, loc) -> forceWrite.contains(tag) || shouldAdd(tag.location()) || this.shouldAdd(loc));

        // A tag that references a tag nobody defines fails to load as a whole ("Couldn't load tag ... as it is
        // missing following references"), which e.g. left #minecraft:mineable/axe empty and made axes slow.
        // So every referenced tag in one of our namespaces is written as well (empty if it has no entries), and
        // references to tags of other mods are optional, as we cannot know if they are present.
        final Set<TagKey<T>> written = new HashSet<>(elementsByTag.keySet());
        tagsByTag.values().forEach(tags -> tags.forEach(pair -> {
            final ResourceLocation id = pair.first.location();
            if (!isOptional(pair.second) && !ResourceLocation.DEFAULT_NAMESPACE.equals(id.getNamespace()) && shouldAdd(id))
                written.add(pair.first);
        }));

        for (TagKey<T> tag : written) {
            final FabricTagProvider<T>.FabricTagBuilder builder = getOrCreateTagBuilder(tag);

            final List<Pair<ResourceLocation, TagEntry>> locs = elementsByTag.getOrDefault(tag, List.of());
            final List<Pair<TagKey<T>, TagEntry>> tags = tagsByTag.getOrDefault(tag, List.of());
            locs.stream().sorted(Comparator.comparing(a -> a.first.toString())).forEach(pair -> {
                if (isOptional(pair.second)) builder.addOptional(pair.first);
                else builder.add(pair.first);
            });

            tags.stream().sorted(Comparator.comparing(a -> a.first.location().toString())).forEach(pair -> {
                if (isOptional(pair.second) || !isKnownTag(pair.first, written)) builder.addOptionalTag(pair.first);
                else builder.forceAddTag(pair.first);
            });
        }
    }

    /**
     * Whether a referenced tag is guaranteed to exist at runtime: Either we write it ourselves, or it is a
     * vanilla tag.
     */
    protected boolean isKnownTag(TagKey<T> tag, Set<TagKey<T>> written) {
        return written.contains(tag) || ResourceLocation.DEFAULT_NAMESPACE.equals(tag.location().getNamespace());
    }
}
