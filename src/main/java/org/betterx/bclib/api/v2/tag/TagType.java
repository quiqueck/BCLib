package org.betterx.bclib.api.v2.tag;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraft.world.level.biome.Biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TagType<T> {
    boolean isFrozen = false;

    public static class RegistryBacked<T> extends Simple<T> {
        private final DefaultedRegistry<T> registry;

        RegistryBacked(DefaultedRegistry<T> registry) {
            super(
                    registry.key(),
                    TagManager.getTagDir(registry.key()),
                    (T element) -> {
                        ResourceLocation id = registry.getKey(element);
                        if (id != registry.getDefaultKey()) {
                            return id;
                        }
                        return null;
                    }
            );
            this.registry = registry;
        }

        @Override
        public TagKey<T> makeTag(ResourceLocation id) {
            initializeTag(id);
            return registry
                    .getTagNames()
                    .filter(tagKey -> tagKey.location().equals(id))
                    .findAny()
                    .orElse(TagKey.create(registry.key(), id));
        }
    }

    public static class Simple<T> extends TagType<T> {
        Simple(ResourceKey<? extends Registry<T>> registry,
               String directory,
               Function<T, ResourceLocation> locationProvider) {
            super(registry, directory, locationProvider);
        }

        public void add(TagKey<T> tagID, T... elements) {
            super.add(tagID, elements);
        }

        public void add(T element, TagKey<T>... tagIDs) {
            super.add(element, tagIDs);
        }

        @Deprecated(forRemoval = true)
        public void add(ResourceLocation tagID, T... elements) {
            super.add(tagID, elements);
        }

        @Deprecated(forRemoval = true)
        public void add(T element, ResourceLocation... tagIDs) {
            super.add(element, tagIDs);
        }
    }

    public static class UnTyped<T> extends TagType<T> {
        UnTyped(ResourceKey<? extends Registry<T>> registry,
                String directory) {
            super(registry, directory, (t) -> {
                throw new RuntimeException("Using Untyped TagType with Type-Dependant access. ");
            });
        }
    }

    public final String directory;
    private final Map<ResourceLocation, Set<TagEntry>> tags = Maps.newConcurrentMap();
    public final ResourceKey<? extends Registry<T>> registryKey;
    private final Function<T, ResourceLocation> locationProvider;

    private TagType(ResourceKey<? extends Registry<T>> registry,
                    String directory,
                    Function<T, ResourceLocation> locationProvider) {
        this.registryKey = registry;
        this.directory = directory;
        this.locationProvider = locationProvider;
    }

    protected void initializeTag(ResourceLocation tagID){
        getSetForTag(tagID);
    }

    public Set<TagEntry> getSetForTag(ResourceLocation tagID) {
        return tags.computeIfAbsent(tagID, k -> Sets.newHashSet());
    }

    public Set<TagEntry> getSetForTag(TagKey<T> tag) {
        if (tag==null) {
            return new HashSet<>();
        }
        return getSetForTag(tag.location());
    }


    /**
     * Get or create a {@link TagKey}.
     *
     * @param id - {@link ResourceLocation} of the tag;
     * @return the corresponding TagKey {@link TagKey<T>}.
     */
    public TagKey<T> makeTag(ResourceLocation id) {
        return creatTagKey(id);
    }

    protected TagKey<T> creatTagKey(ResourceLocation id) {
        initializeTag(id);
        return TagKey.create(registryKey, id);
    }

    /**
     * Get or create a common {@link TagKey} (namespace is 'c').
     *
     * @param name - The name of the Tag;
     * @return the corresponding TagKey {@link TagKey<T>}.
     * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
     */
    public TagKey<T> makeCommonTag(String name) {
        return creatTagKey(new ResourceLocation("c", name));
    }

    public void addUntyped(TagKey<T> tagID, ResourceLocation... elements) {
        if (isFrozen) BCLib.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
        Set<TagEntry> set = getSetForTag(tagID);
        for (ResourceLocation id : elements) {
            if (id != null) {
                set.add(TagEntry.element(id));
            }
        }
    }

    public void addUntyped(ResourceLocation element, TagKey<T>... tagIDs) {
        for (TagKey<T> tagID : tagIDs) {
            addUntyped(tagID, element);
        }
    }

    public void addOtherTags(TagKey<T> tagID, TagKey<T>... tags) {
        if (isFrozen) BCLib.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
        Set<TagEntry> set = getSetForTag(tagID);
        for (TagKey<T> tag : tags) {
            ResourceLocation id = tag.location();
            if (id != null) {
                set.add(TagEntry.tag(id));
            }
        }
    }

    /**
     * Adds one Tag to multiple Elements.
     *
     * @param tagID    {@link TagKey< Biome >} tag ID.
     * @param elements array of Elements to add into tag.
     */
    protected void add(TagKey<T> tagID, T... elements) {
        if (isFrozen) BCLib.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
        Set<TagEntry> set = getSetForTag(tagID);
        for (T element : elements) {
            ResourceLocation id = locationProvider.apply(element);
            if (id != null) {
                set.add(TagEntry.element(id));
            }
        }
    }

    protected void add(T element, TagKey<T>... tagIDs) {
        for (TagKey<T> tagID : tagIDs) {
            add(tagID, element);
        }
    }

    @Deprecated(forRemoval = true)
    protected void add(ResourceLocation tagID, T... elements) {
        if (isFrozen) BCLib.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
        Set<TagEntry> set = getSetForTag(tagID);
        for (T element : elements) {
            ResourceLocation id = locationProvider.apply(element);
            if (id != null) {
                set.add(TagEntry.element(id));
            }
        }
    }

    @Deprecated(forRemoval = true)
    protected void add(T element, ResourceLocation... tagIDs) {
        for (ResourceLocation tagID : tagIDs) {
            add(tagID, element);
        }
    }

    public void forEach(BiConsumer<ResourceLocation, Set<TagEntry>> consumer) {
        tags.forEach(consumer);
    }

    public void apply(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagsMap) {
        if (Registry.BIOME_REGISTRY.equals(registryKey)) InternalBiomeAPI._runBiomeTagAdders();

        //this.isFrozen = true;
        this.forEach((id, ids) -> apply(id, tagsMap.computeIfAbsent(id, key -> Lists.newArrayList()), ids));
    }

    private static List<TagLoader.EntryWithSource> apply(ResourceLocation id,
                                                         List<TagLoader.EntryWithSource> builder,
                                                         Set<TagEntry> ids) {
        ids.forEach(value -> builder.add(new TagLoader.EntryWithSource(value, BCLib.MOD_ID)));
        return builder;
    }
}
