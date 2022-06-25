package org.betterx.bclib.api.v2.tag;

import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.worlds.together.mixin.common.DiggerItemAccessor;
import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.tag.v3.TagRegistry;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @deprecated Replaced by {@link TagManager}
 */
@Deprecated(forRemoval = true)
public class TagAPI {
    private static final Map<String, TagType<?>> TYPES = Maps.newHashMap();

    /**
     * @deprecated Replaced by {@link TagManager#BLOCKS}
     */
    @Deprecated(forRemoval = true)
    public static TagType.RegistryBacked<Block> BLOCKS = registerType(Registry.BLOCK);
    /**
     * @deprecated Replaced by {@link TagManager#ITEMS}
     */
    @Deprecated(forRemoval = true)
    public static TagType.RegistryBacked<Item> ITEMS = registerType(Registry.ITEM);

    /**
     * @deprecated Replaced by {@link TagManager#BIOMES}
     */
    @Deprecated(forRemoval = true)
    public static TagType.Simple<Biome> BIOMES = registerType(
            Registry.BIOME_REGISTRY,
            "tags/worldgen/biome",
            b -> BiomeAPI.getBiomeID(b)
    );

    /**
     * @deprecated Replaced by {@link TagManager#registerType(DefaultedRegistry)}
     */
    @Deprecated(forRemoval = true)
    public static <T> TagType.RegistryBacked<T> registerType(DefaultedRegistry<T> registry) {
        TagType<T> type = new TagType.RegistryBacked<>(registry);
        return (TagType.RegistryBacked<T>) TYPES.computeIfAbsent(type.directory, (dir) -> type);
    }

    /**
     * @deprecated Replaced by {@link TagManager#registerType(Registry, String)}
     */
    @Deprecated(forRemoval = true)
    public static <T> TagType.Simple<T> registerType(Registry<T> registry, String directory) {
        return registerType(registry.key(), directory, (o) -> registry.getKey(o));
    }

    /**
     * @deprecated Replaced by {@link TagManager#registerType(ResourceKey, String, Function)}
     */
    @Deprecated(forRemoval = true)
    public static <T> TagType.Simple<T> registerType(
            ResourceKey<? extends Registry<T>> registry,
            String directory,
            Function<T, ResourceLocation> locationProvider
    ) {
        return (TagType.Simple<T>) TYPES.computeIfAbsent(
                directory,
                (dir) -> new TagType.Simple<>(
                        registry,
                        dir,
                        locationProvider
                )
        );
    }

    /**
     * @deprecated Replaced by {@link TagManager#registerType(ResourceKey, String)}
     */
    @Deprecated(forRemoval = true)
    public static <T> TagType.UnTyped<T> registerType(ResourceKey<? extends Registry<T>> registry, String directory) {
        return (TagType.UnTyped<T>) TYPES.computeIfAbsent(directory, (dir) -> new TagType.UnTyped<>(registry, dir));
    }

    /**
     * @deprecated Replaced by {@link TagRegistry#makeTag(String, String)} on {@link TagManager#BIOMES}
     */
    @Deprecated(forRemoval = true)
    public static TagKey<Biome> makeBiomeTag(String modID, String name) {
        return TagManager.BIOMES.makeTag(new ResourceLocation(modID, name));
    }


    /**
     * @deprecated Replaced by {@link TagRegistry.Biomes#makeStructureTag(String, String)} on {@link TagManager#BIOMES}
     */
    @Deprecated(forRemoval = true)
    public static TagKey<Biome> makeStructureTag(String modID, String name) {
        return TagManager.BIOMES.makeStructureTag(modID, name);
    }


    /**
     * @deprecated Replaced by {@link TagRegistry#makeTag(String, String)} on {@link TagManager#BLOCKS}
     */
    @Deprecated(forRemoval = true)
    public static TagKey<Block> makeBlockTag(String modID, String name) {
        return TagManager.BLOCKS.makeTag(new ResourceLocation(modID, name));
    }

    /**
     * @deprecated Replaced by {@link TagRegistry#makeTag(ResourceLocation)}  on {@link TagManager#BLOCKS}
     */
    @Deprecated(forRemoval = true)
    public static TagKey<Block> makeBlockTag(ResourceLocation id) {
        return TagManager.BLOCKS.makeTag(id);
    }

    /**
     * @deprecated Replaced by {@link TagRegistry#makeTag(String, String)} on {@link TagManager#ITEMS}
     */
    @Deprecated(forRemoval = true)
    public static TagKey<Item> makeItemTag(String modID, String name) {
        return TagManager.ITEMS.makeTag(new ResourceLocation(modID, name));
    }

    /**
     * @deprecated Replaced by {@link TagRegistry#makeTag(ResourceLocation)}  on {@link TagManager#ITEMS}
     */
    @Deprecated(forRemoval = true)
    public static TagKey<Item> makeItemTag(ResourceLocation id) {
        return TagManager.ITEMS.makeTag(id);
    }

    /**
     * @deprecated Replaced by {@link TagRegistry#makeCommonTag(String)}  on {@link TagManager#BLOCKS}
     */
    @Deprecated(forRemoval = true)
    public static TagKey<Block> makeCommonBlockTag(String name) {
        return TagManager.BLOCKS.makeCommonTag(name);
    }

    /**
     * @deprecated Replaced by {@link TagRegistry#makeCommonTag(String)}  on {@link TagManager#ITEMS}
     */
    @Deprecated(forRemoval = true)
    public static TagKey<Item> makeCommonItemTag(String name) {
        return TagManager.ITEMS.makeCommonTag(name);
    }

    /**
     * @deprecated Replaced by {@link TagRegistry#makeCommonTag(String)}  on {@link TagManager#BIOMES}
     */
    @Deprecated(forRemoval = true)
    public static TagKey<Biome> makeCommonBiomeTag(String name) {
        return TagManager.BIOMES.makeCommonTag(name);
    }

    /**
     * Initializes basic tags. Should be called only in BCLib main class.
     */
    @Deprecated(forRemoval = true)
    public static void init() {
    }

    /**
     * Please use {@link TagManager}.BIOMES.add(biome, tagIDs) instead
     *
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    @SafeVarargs
    public static void addBiomeTags(Biome biome, TagKey<Biome>... tagIDs) {
        TagManager.BIOMES.add(biome, tagIDs);
    }


    /**
     * Please use {@link TagManager}.BIOMES.add(tagID, biomes) instead
     *
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    public static void addBiomeTag(TagKey<Biome> tagID, Biome... biomes) {
        TagManager.BIOMES.add(tagID, biomes);
    }


    /**
     * Please use {@link TagManager}.BLOCKS.add(block, tagIDs) instead
     *
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    @SafeVarargs
    public static void addBlockTags(Block block, TagKey<Block>... tagIDs) {
        TagManager.BLOCKS.add(block, tagIDs);
    }


    /**
     * Please use {@link TagManager}.BIOMES.add(tagID, blocks) instead
     *
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    public static void addBlockTag(TagKey<Block> tagID, Block... blocks) {
        TagManager.BLOCKS.add(tagID, blocks);
    }

    /**
     * Please use {@link TagManager}.ITEMS.add(item, tagIDs) instead
     *
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    @SafeVarargs
    public static void addItemTags(ItemLike item, TagKey<Item>... tagIDs) {
        TagManager.ITEMS.add(item.asItem(), tagIDs);
    }

    /**
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    public static void addItemTag(TagKey<Item> tagID, ItemLike... items) {
        for (ItemLike i : items) {
            TagManager.ITEMS.add(i.asItem(), tagID);
        }
    }

    /**
     * Please use {@link TagManager}.ITEMS.add(tagID, items) instead
     *
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    public static void addItemTag(TagKey<Item> tagID, Item... items) {
        TagManager.ITEMS.add(tagID, items);
    }


    @Deprecated(forRemoval = true)
    public static <T> Map<ResourceLocation, List<TagLoader.EntryWithSource>> apply(
            String directory,
            Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagsMap
    ) {
        TagType<?> type = TYPES.get(directory);
        if (type != null) {
            type.apply(tagsMap);
        }
        return tagsMap;
    }


    /**
     * @param stack
     * @param tag
     * @return
     * @deprecated call {@link TagManager#isToolWithMineableTag(ItemStack, TagKey)} instead
     */
    @Deprecated(forRemoval = true)
    public static boolean isToolWithMineableTag(ItemStack stack, TagKey<Block> tag) {
        if (stack.getItem() instanceof DiggerItemAccessor dig) {
            return dig.bclib_getBlockTag().equals(tag);
        }
        return false;
    }
}
