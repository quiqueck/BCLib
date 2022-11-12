package org.betterx.bclib.api.v2;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.blocks.BaseBarrelBlock;
import org.betterx.bclib.blocks.BaseChestBlock;
import org.betterx.bclib.blocks.BaseFurnaceBlock;
import org.betterx.bclib.blocks.BaseSignBlock;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.client.render.BaseChestBlockEntityRenderer;
import org.betterx.bclib.client.render.BaseSignBlockEntityRenderer;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.interfaces.Fuel;
import org.betterx.bclib.interfaces.PostInitable;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.interfaces.TagProvider;
import org.betterx.bclib.interfaces.tools.*;
import org.betterx.bclib.networking.VersionChecker;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.worlds.together.tag.v3.MineableTags;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.registry.FuelRegistry;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Consumer;

public class PostInitAPI {
    private static List<Consumer<Boolean>> postInitFunctions = Lists.newArrayList();
    private static List<TagKey<Block>> blockTags = Lists.newArrayList();
    private static List<TagKey<Item>> itemTags = Lists.newArrayList();

    /**
     * Register a new function which will be called after all mods are initiated. Will be called on both client and server.
     *
     * @param function {@link Consumer} with {@code boolean} parameter ({@code true} for client, {@code false} for server).
     */
    public static void register(Consumer<Boolean> function) {
        postInitFunctions.add(function);
    }

    /**
     * Called in proper BCLib entry points, for internal usage only.
     *
     * @param isClient {@code boolean}, {@code true} for client, {@code false} for server.
     */
    public static void postInit(boolean isClient) {
        BuiltInRegistries.BLOCK.forEach(block -> {
            processBlockCommon(block);
            if (isClient) {
                processBlockClient(block);
            }
        });


        BuiltInRegistries.ITEM.forEach(item -> {
            processItemCommon(item);
        });

        if (postInitFunctions != null) {
            postInitFunctions.forEach(function -> function.accept(isClient));
            postInitFunctions = null;
        }
        blockTags = null;
        itemTags = null;
        InternalBiomeAPI.loadFabricAPIBiomes();
        Configs.BIOMES_CONFIG.saveChanges();

        VersionChecker.startCheck(isClient);
    }

    @Environment(EnvType.CLIENT)
    private static void processBlockClient(Block block) {
        if (block instanceof RenderLayerProvider) {
            BCLRenderLayer layer = ((RenderLayerProvider) block).getRenderLayer();
            if (layer == BCLRenderLayer.CUTOUT) BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
            else if (layer == BCLRenderLayer.TRANSLUCENT)
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.translucent());
        }
        if (block instanceof BaseChestBlock) {
            BaseChestBlockEntityRenderer.registerRenderLayer(block);
        } else if (block instanceof BaseSignBlock) {
            BaseSignBlockEntityRenderer.registerRenderLayer(block);
        }
    }

    private static void processItemCommon(Item item) {
        if (item instanceof TagProvider provider) {
            try {
                provider.addTags(null, itemTags);
            } catch (NullPointerException ex) {
                BCLib.LOGGER.error(item + " probably tried to access blockTags.", ex);
            }
            itemTags.forEach(tag -> TagManager.ITEMS.add(tag, item));
            itemTags.clear();
        }
    }

    private static void processBlockCommon(Block block) {
        if (block instanceof PostInitable) {
            ((PostInitable) block).postInit();
        }
        if (block instanceof BaseChestBlock) {
            BaseBlockEntities.CHEST.registerBlock(block);
        } else if (block instanceof BaseSignBlock) {
            BaseBlockEntities.SIGN.registerBlock(block);
        } else if (block instanceof BaseBarrelBlock) {
            BaseBlockEntities.BARREL.registerBlock(block);
        } else if (block instanceof BaseFurnaceBlock) {
            BaseBlockEntities.FURNACE.registerBlock(block);
        }
        if (!(block instanceof PreventMineableAdd)) {
            if (block instanceof AddMineableShears) {
                TagManager.BLOCKS.add(block, MineableTags.SHEARS);
            }
            if (block instanceof AddMineableAxe) {
                TagManager.BLOCKS.add(block, MineableTags.AXE);
            }
            if (block instanceof AddMineablePickaxe) {
                TagManager.BLOCKS.add(block, MineableTags.PICKAXE);
            }
            if (block instanceof AddMineableShovel) {
                TagManager.BLOCKS.add(block, MineableTags.SHOVEL);
            }
            if (block instanceof AddMineableHoe) {
                TagManager.BLOCKS.add(block, MineableTags.HOE);
            }
            if (block instanceof AddMineableSword) {
                TagManager.BLOCKS.add(block, MineableTags.SWORD);
            }
            if (block instanceof AddMineableHammer) {
                TagManager.BLOCKS.add(block, MineableTags.HAMMER);
            }
        }
        if (block instanceof TagProvider) {
            ((TagProvider) block).addTags(blockTags, itemTags);
            blockTags.forEach(tag -> TagManager.BLOCKS.add(tag, block));
            itemTags.forEach(tag -> TagManager.ITEMS.add(tag, block.asItem()));
            blockTags.clear();
            itemTags.clear();
        }

        if (block instanceof Fuel fl) {
            FuelRegistry.INSTANCE.add(block, fl.getFuelTime());
        }
    }
}
