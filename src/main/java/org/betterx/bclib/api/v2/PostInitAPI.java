package org.betterx.bclib.api.v2;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.behaviours.interfaces.*;
import org.betterx.bclib.blocks.BaseBarrelBlock;
import org.betterx.bclib.blocks.BaseChestBlock;
import org.betterx.bclib.blocks.BaseFurnaceBlock;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.client.render.BaseChestBlockEntityRenderer;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.interfaces.Fuel;
import org.betterx.bclib.interfaces.PostInitable;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.interfaces.TagProvider;
import org.betterx.bclib.interfaces.tools.*;
import org.betterx.bclib.items.tool.*;
import org.betterx.bclib.networking.VersionChecker;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.worlds.together.tag.v3.*;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;

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

        if (item instanceof BaseShovelItem) {
            TagManager.ITEMS.add(item, ToolTags.FABRIC_SHOVELS, ItemTags.SHOVELS);
        } else if (item instanceof BaseSwordItem) {
            TagManager.ITEMS.add(item, ToolTags.FABRIC_SWORDS, ItemTags.SWORDS);
        } else if (item instanceof BasePickaxeItem) {
            TagManager.ITEMS.add(item, ToolTags.FABRIC_PICKAXES, ItemTags.PICKAXES);
        } else if (item instanceof BaseAxeItem) {
            TagManager.ITEMS.add(item, ToolTags.FABRIC_AXES, ItemTags.AXES);
        } else if (item instanceof BaseHoeItem) {
            TagManager.ITEMS.add(item, ToolTags.FABRIC_HOES, ItemTags.HOES);
        } else if (item instanceof BaseShearsItem) {
            TagManager.ITEMS.add(item, ToolTags.FABRIC_SHEARS, CommonItemTags.SHEARS);
            DispenserBlock.registerBehavior(item.asItem(), new ShearsDispenseItemBehavior());
        }
    }

    private static void processBlockCommon(Block block) {
        //TODO: Some of this only needs to run on DataGen, add a special PostDataGenAPI for that
        final Item item = block.asItem();
        if (block instanceof PostInitable) {
            ((PostInitable) block).postInit();
        }

        if (block instanceof TagProvider) {
            ((TagProvider) block).addTags(blockTags, itemTags);
            blockTags.forEach(tag -> TagManager.BLOCKS.add(tag, block));
            if (item != null && item != Items.AIR)
                itemTags.forEach(tag -> TagManager.ITEMS.add(tag, item));
            blockTags.clear();
            itemTags.clear();
        }

        if (block instanceof BaseChestBlock) {
            BaseBlockEntities.CHEST.registerBlock(block);
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
                if (!TagManager.BLOCKS.contains(BlockTags.WOODEN_DOORS, block)
                        && !TagManager.BLOCKS.contains(BlockTags.WOODEN_BUTTONS, block)
                        && !TagManager.BLOCKS.contains(BlockTags.WOODEN_SLABS, block)
                        && !TagManager.BLOCKS.contains(BlockTags.WOODEN_FENCES, block)
                        && !TagManager.BLOCKS.contains(BlockTags.WOODEN_STAIRS, block)
                        && !TagManager.BLOCKS.contains(BlockTags.WOODEN_PRESSURE_PLATES, block)
                        && !TagManager.BLOCKS.contains(BlockTags.WOODEN_TRAPDOORS, block)
                        && !TagManager.BLOCKS.contains(CommonBlockTags.WOODEN_BARREL, block)
                        && !TagManager.BLOCKS.contains(CommonBlockTags.WOODEN_CHEST, block)
                        && !TagManager.BLOCKS.contains(CommonBlockTags.WOODEN_COMPOSTER, block)
                        && !TagManager.BLOCKS.contains(CommonBlockTags.WORKBENCHES, block)
                        && !TagManager.BLOCKS.contains(BlockTags.SIGNS, block)
                        && !TagManager.BLOCKS.contains(BlockTags.PLANKS, block)
                        && !TagManager.BLOCKS.contains(BlockTags.LOGS, block)
                        && !TagManager.BLOCKS.contains(BlockTags.FENCE_GATES, block)
                        && !TagManager.BLOCKS.contains(BlockTags.ALL_HANGING_SIGNS, block)
                        && !TagManager.BLOCKS.contains(CommonBlockTags.WORKBENCHES, block)
                        && !TagManager.BLOCKS.contains(CommonBlockTags.BOOKSHELVES, block)
                ) {
                    TagManager.BLOCKS.add(block, MineableTags.AXE);
                }
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

        if (block instanceof BehaviourCompostable c) {
            if (item != null && item != Items.AIR) {
                TagManager.ITEMS.add(block, CommonItemTags.COMPOSTABLE);
                ComposterAPI.allowCompost(c.compostingChance(), item);
            } else if (BCLib.isDatagen() && Configs.MAIN_CONFIG.verboseLogging()) {
                BCLib.LOGGER.warning("Block " + block + " has compostable behaviour but no item!");
            }
        }

        if (block instanceof BehaviourWaterPlantLike) {
            TagManager.BLOCKS.add(block, CommonBlockTags.WATER_PLANT);
        }

        if (block instanceof BehaviourPlant || block instanceof BehaviourShearablePlant) {
            TagManager.BLOCKS.add(block, CommonBlockTags.PLANT);
        }


        if (block instanceof BehaviourSeedLike) {
            TagManager.BLOCKS.add(block, CommonBlockTags.SEEDS);
            if (item != null && item != Items.AIR) {
                TagManager.ITEMS.add(block, CommonItemTags.SEEDS);
            }
        }

        if (block instanceof BehaviourSaplingLike) {
            TagManager.BLOCKS.add(block, CommonBlockTags.SAPLINGS, BlockTags.SAPLINGS);
            if (item != null && item != Items.AIR) {
                TagManager.ITEMS.add(block, CommonItemTags.SAPLINGS, ItemTags.SAPLINGS);
            }
        }

        if (block instanceof BehaviourClimable c) {
            TagManager.BLOCKS.add(block, BlockTags.CLIMBABLE);
        }

        if (block instanceof BehaviourLeaves) {
            TagManager.BLOCKS.add(block, BlockTags.LEAVES, CommonBlockTags.LEAVES);
            if (item != null && item != Items.AIR)
                TagManager.ITEMS.add(item, ItemTags.LEAVES, CommonItemTags.LEAVES);
        }

        if (block instanceof BehaviourImmobile) {
            TagManager.BLOCKS.add(block, CommonBlockTags.IMMOBILE);
        }

        if (block instanceof BehaviourObsidian) {
            TagManager.BLOCKS.add(block, CommonBlockTags.IS_OBSIDIAN);
        }

        if (block instanceof BehaviourPortalFrame) {
            TagManager.BLOCKS.add(block, CommonBlockTags.NETHER_PORTAL_FRAME);
        }

        if (block instanceof BehaviourOre) {
            TagManager.BLOCKS.add(block, CommonBlockTags.ORES);
        }

        if (block instanceof Fuel fl) {
            FuelRegistry.INSTANCE.add(block, fl.getFuelTime());
        }

        if (BCLib.isDatagen()) {
            final ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);
            if (!location.getNamespace().equals("minecraft")) {
                if (!(block instanceof HasMinableBehaviour) && block.defaultBlockState()
                                                                    .requiresCorrectToolForDrops()) {
                    BCLib.LOGGER.error("Block " + block + "(" + block.getClass() + ")" + " has no mineable behaviour!");
                }
            }
        }
    }
}
