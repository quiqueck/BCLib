package org.betterx.worlds.together.tag.v3;

import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.mixin.common.DiggerItemAccessor;
import org.betterx.worlds.together.world.event.WorldEventsImpl;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;

public class TagManager {
    private static final Map<String, TagRegistry<?>> TYPES = Maps.newHashMap();

    public static TagRegistry.RegistryBacked<Block> BLOCKS = registerType(BuiltInRegistries.BLOCK);
    public static TagRegistry.Items ITEMS = registerItem();
    public static TagRegistry.Biomes BIOMES = registerBiome();

    public static <T> TagRegistry.RegistryBacked<T> registerType(DefaultedRegistry<T> registry) {
        TagRegistry<T> type = new TagRegistry.RegistryBacked<>(registry);
        return (TagRegistry.RegistryBacked<T>) TYPES.computeIfAbsent(type.directory, (dir) -> type);
    }

    public static TagRegistry.Items registerItem() {
        TagRegistry.Items type = new TagRegistry.Items();
        return (TagRegistry.Items) TYPES.computeIfAbsent(type.directory, (dir) -> type);
    }

    public static <T> TagRegistry.Simple<T> registerType(Registry<T> registry, String directory) {
        return registerType(registry.key(), directory, (o) -> registry.getKey(o));
    }

    public static <T> TagRegistry.Simple<T> registerType(
            ResourceKey<? extends Registry<T>> registry,
            String directory,
            Function<T, ResourceLocation> locationProvider
    ) {
        return (TagRegistry.Simple<T>) TYPES.computeIfAbsent(
                directory,
                (dir) -> new TagRegistry.Simple<>(
                        registry,
                        dir,
                        locationProvider
                )
        );
    }

    static TagRegistry.Biomes registerBiome() {
        return (TagRegistry.Biomes) TYPES.computeIfAbsent(
                "tags/worldgen/biome",
                (dir) -> new TagRegistry.Biomes(
                        dir,
                        b -> WorldGenUtil.getBiomeID(b)
                )
        );
    }

    public static <T> TagRegistry.UnTyped<T> registerType(
            ResourceKey<? extends Registry<T>> registry,
            String directory
    ) {
        return (TagRegistry.UnTyped<T>) TYPES.computeIfAbsent(
                directory,
                (dir) -> new TagRegistry.UnTyped<>(registry, dir)
        );
    }

    /**
     * Initializes basic tags. Should be called only in BCLib main class.
     */
    @ApiStatus.Internal
    public static void ensureStaticallyLoaded() {
        CommonItemTags.prepareTags();
        CommonBlockTags.prepareTags();
        CommonBiomeTags.prepareTags();
        MineableTags.prepareTags();
        ToolTags.prepareTags();
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
    @ApiStatus.Internal
    public static <T> Map<ResourceLocation, List<TagLoader.EntryWithSource>> apply(
            String directory,
            Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagsMap
    ) {
        WorldEventsImpl.BEFORE_ADDING_TAGS.emit(e -> e.apply(directory, tagsMap));

        TagRegistry<?> type = TYPES.get(directory);
        if (type != null) {
            type.apply(tagsMap);
        }

        return tagsMap;
    }


    public static boolean isToolWithMineableTag(ItemStack stack, TagKey<Block> tag) {
        if (stack.getItem() instanceof DiggerItemAccessor dig) {
            return dig.bclib_getBlockTag() == tag;
        }
        return false;
    }
}
