package org.betterx.bclib.api.v2.tag;

import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.mixin.common.DiggerItemAccessor;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TagAPI {
    private static final Map<String, TagType<?>> TYPES = Maps.newHashMap();

    public static TagType.RegistryBacked<Block> BLOCKS = registerType(Registry.BLOCK);
    public static TagType.RegistryBacked<Item> ITEMS = registerType(Registry.ITEM);
    public static TagType.Simple<Biome> BIOMES = registerType(
            Registry.BIOME_REGISTRY,
            "tags/worldgen/biome",
            b -> BiomeAPI.getBiomeID(b)
    );

    public static <T> TagType.RegistryBacked<T> registerType(DefaultedRegistry<T> registry) {
        TagType<T> type = new TagType.RegistryBacked<>(registry);
        return (TagType.RegistryBacked<T>) TYPES.computeIfAbsent(type.directory, (dir) -> type);
    }

    public static <T> TagType.Simple<T> registerType(Registry<T> registry, String directory) {
        return registerType(registry.key(), directory, (o) -> registry.getKey(o));
    }

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

    public static <T> TagType.UnTyped<T> registerType(ResourceKey<? extends Registry<T>> registry, String directory) {
        return (TagType.UnTyped<T>) TYPES.computeIfAbsent(directory, (dir) -> new TagType.UnTyped<>(registry, dir));
    }

    /**
     * Get or create {@link Block} {@link TagKey} with mod namespace.
     *
     * @param modID - {@link String} mod namespace (mod id);
     * @param name  - {@link String} tag name.
     * @return {@link Block} {@link TagKey}.
     */
    public static TagKey<Biome> makeBiomeTag(String modID, String name) {
        return BIOMES.makeTag(new ResourceLocation(modID, name));
    }

    public static TagKey<Biome> makeStructureTag(String modID, String name) {
        return TagAPI.makeBiomeTag(modID, "has_structure/" + name);
    }


    /**
     * Get or create {@link Block} {@link TagKey} with mod namespace.
     *
     * @param modID - {@link String} mod namespace (mod id);
     * @param name  - {@link String} tag name.
     * @return {@link Block} {@link TagKey}.
     */
    public static TagKey<Block> makeBlockTag(String modID, String name) {
        return BLOCKS.makeTag(new ResourceLocation(modID, name));
    }

    /**
     * Get or create {@link Block} {@link TagKey} with mod namespace.
     *
     * @param id - {@link String} id for the tag;
     * @return {@link Block} {@link TagKey}.
     */
    public static TagKey<Block> makeBlockTag(ResourceLocation id) {
        return BLOCKS.makeTag(id);
    }

    /**
     * Get or create {@link Item} {@link TagKey} with mod namespace.
     *
     * @param modID - {@link String} mod namespace (mod id);
     * @param name  - {@link String} tag name.
     * @return {@link Item} {@link TagKey}.
     */
    public static TagKey<Item> makeItemTag(String modID, String name) {
        return ITEMS.makeTag(new ResourceLocation(modID, name));
    }

    /**
     * Get or create {@link Item} {@link TagKey} with mod namespace.
     *
     * @param id - {@link String} id for the tag;
     * @return {@link Item} {@link TagKey}.
     */
    public static TagKey<Item> makeItemTag(ResourceLocation id) {
        return ITEMS.makeTag(id);
    }

    /**
     * Get or create {@link Block} {@link TagKey}.
     *
     * @param name - {@link String} tag name.
     * @return {@link Block} {@link TagKey}.
     * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
     */
    public static TagKey<Block> makeCommonBlockTag(String name) {
        return BLOCKS.makeCommonTag(name);
    }

    /**
     * Get or create {@link Item} {@link TagKey}.
     *
     * @param name - {@link String} tag name.
     * @return {@link Item} {@link TagKey}.
     * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
     */
    public static TagKey<Item> makeCommonItemTag(String name) {
        return ITEMS.makeCommonTag(name);
    }

    public static TagKey<Biome> makeCommonBiomeTag(String name) {
        return BIOMES.makeCommonTag(name);
    }

    /**
     * Initializes basic tags. Should be called only in BCLib main class.
     */
    public static void init() {
        addBlockTag(CommonBlockTags.BOOKSHELVES, Blocks.BOOKSHELF);
        addBlockTag(CommonBlockTags.CHEST, Blocks.CHEST);
        addItemTag(CommonItemTags.CHEST, Items.CHEST);
        addItemTag(CommonItemTags.IRON_INGOTS, Items.IRON_INGOT);
        addItemTag(CommonItemTags.FURNACES, Blocks.FURNACE);
    }

    /**
     * Adds multiple Tags to one Biome.
     *
     * @param tagIDs array of {@link TagKey<Biome>} tag IDs.
     * @param biome  The {@link Biome} to add tag.
     */
    @SafeVarargs
    public static void addBiomeTags(Biome biome, TagKey<Biome>... tagIDs) {
        BIOMES.add(biome, tagIDs);
    }

    /**
     * Adds one Tag to multiple Biomes.
     *
     * @param tagID  {@link TagKey<Biome>} tag ID.
     * @param biomes array of {@link Biome} to add into tag.
     */
    public static void addBiomeTag(TagKey<Biome> tagID, Biome... biomes) {
        BIOMES.add(tagID, biomes);
    }


    /**
     * Adds multiple Tags to one Block.
     *
     * @param tagIDs array of {@link TagKey<Block>} tag IDs.
     * @param block  The {@link Block} to add tag.
     */
    @SafeVarargs
    public static void addBlockTags(Block block, TagKey<Block>... tagIDs) {
        BLOCKS.add(block, tagIDs);
    }


    /**
     * Adds one Tag to multiple Blocks.
     *
     * @param tagID  {@link TagKey<Block>} tag ID.
     * @param blocks array of {@link Block} to add into tag.
     */
    public static void addBlockTag(TagKey<Block> tagID, Block... blocks) {
        BLOCKS.add(tagID, blocks);
    }

    /**
     * Adds multiple Tags to one Item.
     *
     * @param tagIDs array of {@link TagKey<Item>} tag IDs.
     * @param item   The {@link Item} to add tag.
     */
    @SafeVarargs
    public static void addItemTags(ItemLike item, TagKey<Item>... tagIDs) {
        ITEMS.add(item.asItem(), tagIDs);
    }

    /**
     * Adds one Tag to multiple Items.
     *
     * @param tagID {@link TagKey<Item>} tag ID.
     * @param items array of {@link ItemLike} to add into tag.
     */
    public static void addItemTag(TagKey<Item> tagID, ItemLike... items) {
        for (ItemLike i : items) {
            ITEMS.add(i.asItem(), tagID);
        }
    }

    /**
     * Adds one Tag to multiple Items.
     *
     * @param tagID {@link TagKey<Item>} tag ID.
     * @param items array of {@link ItemLike} to add into tag.
     */
    public static void addItemTag(TagKey<Item> tagID, Item... items) {
        ITEMS.add(tagID, items);
    }


    /**
     * Automatically called in {@link net.minecraft.tags.TagLoader#loadAndBuild(ResourceManager)}.
     * <p>
     * In most cases there is no need to call this Method manually.
     *
     * @param directory The name of the Tag-directory. Should be either <i>"tags/blocks"</i> or
     *                  <i>"tags/items"</i>.
     * @param tagsMap   The map that will hold the registered Tags
     * @return The {@code tagsMap} Parameter.
     */
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


    public static boolean isToolWithMineableTag(ItemStack stack, TagKey<Block> tag) {
        if (stack.getItem() instanceof DiggerItemAccessor dig) {
            return dig.bclib_getBlockTag().equals(tag);
        }
        return false;
    }
}
