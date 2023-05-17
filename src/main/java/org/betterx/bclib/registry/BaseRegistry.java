package org.betterx.bclib.registry;

import org.betterx.bclib.config.PathConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

public abstract class BaseRegistry<T> {
    private static final List<BaseRegistry<?>> REGISTRIES = Lists.newArrayList();
    private static final Map<String, List<Item>> MOD_BLOCK_ITEMS = Maps.newHashMap();
    private static final Map<String, List<Block>> MOD_BLOCKS = Maps.newHashMap();
    private static final Map<String, List<Item>> MOD_ITEMS = Maps.newHashMap();
    protected final PathConfig config;

    protected BaseRegistry(PathConfig config) {
        this.config = config;
        REGISTRIES.add(this);
    }

    public abstract T register(ResourceLocation objId, T obj);

    public abstract void registerItem(ResourceLocation id, Item item);

    public Item.Properties makeItemSettings() {
        Item.Properties properties = new Item.Properties();
        return properties;
    }

    private void registerInternal() {
    }

    public static Map<String, List<Item>> getRegisteredBlocks() {
        return MOD_BLOCK_ITEMS;
    }

    public static Map<String, List<Item>> getRegisteredItems() {
        return MOD_ITEMS;
    }

    public static List<Item> getModBlockItems(String modId) {
        if (MOD_BLOCK_ITEMS.containsKey(modId)) {
            return MOD_BLOCK_ITEMS.get(modId);
        }
        List<Item> modBlocks = Lists.newArrayList();
        MOD_BLOCK_ITEMS.put(modId, modBlocks);
        return modBlocks;
    }

    public static List<Item> getModItems(String modId) {
        if (MOD_ITEMS.containsKey(modId)) {
            return MOD_ITEMS.get(modId);
        }
        List<Item> modBlocks = Lists.newArrayList();
        MOD_ITEMS.put(modId, modBlocks);
        return modBlocks;
    }

    public static List<Block> getModBlocks(String modId) {
        if (MOD_BLOCKS.containsKey(modId)) {
            return MOD_BLOCKS.get(modId);
        }
        List<Block> modBlocks = Lists.newArrayList();
        MOD_BLOCKS.put(modId, modBlocks);
        return modBlocks;
    }

    @ApiStatus.Internal
    public static void register() {
        REGISTRIES.forEach(BaseRegistry::registerInternal);
    }
}
