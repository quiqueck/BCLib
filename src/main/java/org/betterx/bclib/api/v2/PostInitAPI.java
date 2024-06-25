package org.betterx.bclib.api.v2;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.behaviours.interfaces.BehaviourCompostable;
import org.betterx.bclib.blocks.BaseBarrelBlock;
import org.betterx.bclib.blocks.BaseChestBlock;
import org.betterx.bclib.blocks.BaseFurnaceBlock;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.client.render.BaseChestBlockEntityRenderer;
import org.betterx.bclib.interfaces.PostInitable;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.items.tool.BaseShearsItem;
import org.betterx.bclib.registry.BaseBlockEntities;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;

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
        }
    }

    private static void processItemCommon(Item item) {
        if (item instanceof BaseShearsItem) {
            DispenserBlock.registerBehavior(item.asItem(), new ShearsDispenseItemBehavior());
        }
    }

    private static void processBlockCommon(Block block) {
        final Item item = block.asItem();
        if (block instanceof PostInitable) {
            ((PostInitable) block).postInit();
        }

        if (block instanceof BehaviourCompostable c) {
            if (item != null && item != Items.AIR) {
                ComposterAPI.allowCompost(c.compostingChance(), item);
            } else if (BCLib.isDatagen()) {
                BCLib.LOGGER.verbose("Block " + block + " has compostable behaviour but no item!");
            }
        }

        if (block instanceof BaseChestBlock) {
            BaseBlockEntities.CHEST.registerBlock(block);
        } else if (block instanceof BaseBarrelBlock) {
            BaseBlockEntities.BARREL.registerBlock(block);
        } else if (block instanceof BaseFurnaceBlock) {
            BaseBlockEntities.FURNACE.registerBlock(block);
        }
    }
}
