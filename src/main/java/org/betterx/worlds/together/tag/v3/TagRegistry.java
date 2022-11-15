package org.betterx.worlds.together.tag.v3;

import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TagRegistry<T> {
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

    public static class Simple<T> extends TagRegistry<T> {
        Simple(
                ResourceKey<? extends Registry<T>> registry,
                String directory,
                Function<T, ResourceLocation> locationProvider
        ) {
            super(registry, directory, locationProvider);
        }

        @SafeVarargs
        public final void add(TagKey<T> tagID, T... elements) {
            super.add(tagID, elements);
        }

        @SafeVarargs
        public final void add(T element, TagKey<T>... tagIDs) {
            super.add(element, tagIDs);
        }
    }

    public static class Biomes extends Simple<Biome> {
        Biomes(String directory, Function<Biome, ResourceLocation> locationProvider) {
            super(Registry.BIOME_REGISTRY, directory, locationProvider);
        }

        /**
         * Adds one Tag to multiple Elements.
         *
         * @param tagID    {@link TagKey< Biome >} tag ID.
         * @param elements array of Elements to add into tag.
         */
        public void add(TagKey<Biome> tagID, ResourceKey<Biome>... elements) {
            if (isFrozen) WorldsTogether.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
            Set<TagEntry> set = getSetForTag(tagID);
            for (ResourceKey<Biome> element : elements) {
                ResourceLocation id = element.location();
                if (id != null) {
                    set.add(TagEntry.element(id));
                }
            }
        }

        public TagKey<Biome> makeStructureTag(String modID, String name) {
            return makeTag(modID, "has_structure/" + name);
        }

        public void apply(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagsMap) {
            super.apply(tagsMap);
        }
    }

    public static class Items extends RegistryBacked<Item> {

        Items() {
            super(Registry.ITEM);
        }

        @SafeVarargs
        public final void add(TagKey<Item> tagID, ItemLike... elements) {
            for (ItemLike element : elements) {
                add(tagID, element.asItem());
            }
        }

        @SafeVarargs
        public final void add(ItemLike element, TagKey<Item>... tagIDs) {
            super.add(element.asItem(), tagIDs);
        }
    }

    public static class UnTyped<T> extends TagRegistry<T> {
        UnTyped(
                ResourceKey<? extends Registry<T>> registry,
                String directory
        ) {
            super(registry, directory, (t) -> {
                throw new RuntimeException("Using Untyped TagType with Type-Dependant access. ");
            });
        }
    }

    public final String directory;
    private final Map<ResourceLocation, Set<TagEntry>> tags = Maps.newConcurrentMap();
    public final ResourceKey<? extends Registry<T>> registryKey;
    private final Function<T, ResourceLocation> locationProvider;

    private TagRegistry(
            ResourceKey<? extends Registry<T>> registry,
            String directory,
            Function<T, ResourceLocation> locationProvider
    ) {
        this.registryKey = registry;
        this.directory = directory;
        this.locationProvider = locationProvider;
    }

    protected void initializeTag(ResourceLocation tagID) {
        getSetForTag(tagID);
    }

    public Set<TagEntry> getSetForTag(ResourceLocation tagID) {
        return tags.computeIfAbsent(tagID, k -> Sets.newHashSet());
    }

    public Set<TagEntry> getSetForTag(TagKey<T> tag) {
        if (tag == null) {
            return new HashSet<>();
        }
        return getSetForTag(tag.location());
    }

    /**
     * Get or create a {@link TagKey}.
     *
     * @param modId - {@link String} mod namespace (mod id);
     * @param name  - {@link String} tag name.
     * @return the corresponding TagKey {@link TagKey<T>}.
     */
    public TagKey<T> makeTag(String modId, String name) {
        return makeTag(new ResourceLocation(modId, name));
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

    public TagKey<T> makeTogetherTag(String name) {
        return creatTagKey(WorldsTogether.makeID(name));
    }

    public void addUntyped(TagKey<T> tagID, ResourceLocation... elements) {
        if (isFrozen) WorldsTogether.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
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
        if (isFrozen) WorldsTogether.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
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
        if (isFrozen) WorldsTogether.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
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

    public void forEach(BiConsumer<ResourceLocation, Set<TagEntry>> consumer) {
        tags.forEach(consumer);
    }

    public void apply(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagsMap) {

        //this.isFrozen = true;
        this.forEach((id, ids) -> apply(id, tagsMap.computeIfAbsent(id, key -> Lists.newArrayList()), ids));
    }

    private static List<TagLoader.EntryWithSource> apply(
            ResourceLocation id,
            List<TagLoader.EntryWithSource> builder,
            Set<TagEntry> ids
    ) {
        ids.forEach(value -> builder.add(new TagLoader.EntryWithSource(value, WorldsTogether.MOD_ID)));
        return builder;
    }
}
