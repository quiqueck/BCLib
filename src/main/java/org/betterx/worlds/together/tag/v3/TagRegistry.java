package org.betterx.worlds.together.tag.v3;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.TriConsumer;
import org.betterx.bclib.util.Pair;
import org.betterx.worlds.together.WorldsTogether;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
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
            final TagKey<T> tag = registry
                    .getTagNames()
                    .filter(tagKey -> tagKey.location().equals(id))
                    .findAny()
                    .orElse(TagKey.create(registry.key(), id));
            initializeTag(tag);
            return tag;
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
        public final void addOptional(TagKey<T> tagID, T... elements) {
            super.addOptional(tagID, elements);
        }

        @SafeVarargs
        public final void add(T element, TagKey<T>... tagIDs) {
            super.add(element, tagIDs);
        }

        public final boolean contains(TagKey<T> tagID, T element) {
            return super.contains(tagID, element);
        }
    }

    public static class Biomes extends Simple<Biome> {
        Biomes(String directory, Function<Biome, ResourceLocation> locationProvider) {
            super(Registries.BIOME, directory, locationProvider);
        }

        /**
         * Adds one Tag to multiple Elements.
         *
         * @param tagID    {@link TagKey<Biome>} tag ID.
         * @param elements array of Elements to add into tag.
         */
        public void add(TagKey<Biome> tagID, ResourceKey<Biome>... elements) {
            add(tagID, false, elements);
        }

        public void addOptional(TagKey<Biome> tagID, ResourceKey<Biome>... elements) {
            add(tagID, true, elements);
        }

        void add(TagKey<Biome> tagID, boolean optional, ResourceKey<Biome>... elements) {
            if (isFrozen) WorldsTogether.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
            synchronized (this) {
                Set<TagEntry> set = getSetForTag(tagID);
                for (ResourceKey<Biome> element : elements) {
                    ResourceLocation id = element.location();

                    //only add if the set doesn't already contain the element
                    for (TagEntry tagEntry : set) {
                        if (!tagEntry.elementOrTag().tag() && tagEntry.elementOrTag().id().equals(id)) {
                            id = null;
                            break;
                        }
                    }

                    if (id != null) {
                        set.add(optional ? TagEntry.optionalElement(id) : TagEntry.element(id));
                    }
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
            super(BuiltInRegistries.ITEM);
        }

        @SafeVarargs
        public final void add(TagKey<Item> tagID, ItemLike... elements) {
            for (ItemLike element : elements) {
                add(tagID, element.asItem());
            }
        }

        @SafeVarargs
        public final void addOptional(TagKey<Item> tagID, ItemLike... elements) {
            for (ItemLike element : elements) {
                addOptional(tagID, element.asItem());
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
    private final Map<TagKey<T>, Set<TagEntry>> tags = Maps.newConcurrentMap();
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

    protected void initializeTag(TagKey<T> tag) {
        getSetForTag(tag);
    }


    public Set<TagEntry> getSetForTag(TagKey<T> tag) {
        if (tag == null) {
            return new HashSet<>();
        }
        return tags.computeIfAbsent(tag, k -> Sets.newHashSet());
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
        final TagKey<T> tag = TagKey.create(registryKey, id);
        initializeTag(tag);
        return tag;
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
        addOtherTags(tagID, false, tags);
    }

    public void addOptionalOtherTags(TagKey<T> tagID, TagKey<T>... tags) {
        addOtherTags(tagID, true, tags);
    }

    void addOtherTags(TagKey<T> tagID, boolean optional, TagKey<T>... tags) {
        if (isFrozen) WorldsTogether.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
        Set<TagEntry> set = getSetForTag(tagID);
        for (TagKey<T> tag : tags) {
            ResourceLocation id = tag.location();
            if (id != null) {
                set.add(optional ? TagEntry.optionalTag(id) : TagEntry.tag(id));
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
        add(tagID, false, elements);
    }

    protected void addOptional(TagKey<T> tagID, T... elements) {
        add(tagID, true, elements);
    }

    protected void add(TagKey<T> tagID, boolean optional, T... elements) {
        if (isFrozen) WorldsTogether.LOGGER.warning("Adding Tag " + tagID + " after the API was frozen.");
        Set<TagEntry> set = getSetForTag(tagID);
        for (T element : elements) {
            ResourceLocation id = locationProvider.apply(element);

            //only add if the set doesn't already contain the element
            for (TagEntry tagEntry : set) {
                if (!tagEntry.elementOrTag().tag() && tagEntry.elementOrTag().id().equals(id)) {
                    id = null;
                    break;
                }
            }

            if (id != null) {
                set.add(optional ? TagEntry.optionalElement(id) : TagEntry.element(id));
            }
        }
    }

    protected boolean contains(TagKey<T> tagID, T element) {
        final Set<TagEntry> set = getSetForTag(tagID);
        final ResourceLocation id = locationProvider.apply(element);
        if (id != null) {
            for (var entry : set)
                if (!entry.elementOrTag().tag()) {
                    if (id.equals(entry.elementOrTag().id()))
                        return true;
                }
        }
        return false;
    }

    protected void add(T element, TagKey<T>... tagIDs) {
        for (TagKey<T> tagID : tagIDs) {
            add(tagID, element);
        }
    }

    public void forEach(BiConsumer<ResourceLocation, Set<TagEntry>> consumer) {
        tags.forEach((a, b) -> consumer.accept(a.location(), b));
    }

    public void forEachTag(TriConsumer<TagKey<T>, List<ResourceLocation>, List<TagKey<T>>> consumer) {
        forEachTag(consumer, null);
    }

    public void forEachTag(
            TriConsumer<TagKey<T>, List<ResourceLocation>, List<TagKey<T>>> consumer,
            BiPredicate<TagKey<T>, ResourceLocation> allow
    ) {
        tags.forEach((tag, set) -> {
            List<ResourceLocation> locations = new LinkedList<>();
            List<TagKey<T>> tags = new LinkedList<>();

            set.forEach(e -> {
                ExtraCodecs.TagOrElementLocation t = e.elementOrTag();
                if (allow == null || allow.test(tag, t.id())) {
                    if (t.tag()) {
                        tags.add(TagKey.create(registryKey, t.id()));
                    } else {
                        locations.add(t.id());
                    }
                }
            });

            consumer.accept(tag, locations, tags);
        });
    }

    public void forEachEntry(
            TriConsumer<TagKey<T>, List<Pair<ResourceLocation, TagEntry>>, List<Pair<TagKey<T>, TagEntry>>> consumer,
            BiPredicate<TagKey<T>, ResourceLocation> allow
    ) {
        tags.forEach((tag, set) -> {
            List<Pair<ResourceLocation, TagEntry>> locations = new LinkedList<>();
            List<Pair<TagKey<T>, TagEntry>> tags = new LinkedList<>();


            set.forEach(e -> {
                ExtraCodecs.TagOrElementLocation t = e.elementOrTag();
                if (allow == null || allow.test(tag, t.id())) {
                    if (t.tag()) {
                        tags.add(new Pair<>(TagKey.create(registryKey, t.id()), e));
                    } else {
                        locations.add(new Pair<>(t.id(), e));
                    }
                }
            });

            consumer.accept(tag, locations, tags);
        });
    }

    public void apply(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagsMap) {
        //this.isFrozen = true;
        if (BCLib.isDatagen()) {
            this.forEach((id, ids) -> apply(id, tagsMap.computeIfAbsent(id, key -> Lists.newArrayList()), ids));
        } else {
            tags.clear();
        }
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
