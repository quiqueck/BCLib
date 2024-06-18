package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.tag.BCLBlockTags;
import org.betterx.bclib.behaviours.interfaces.*;
import org.betterx.bclib.blocks.BaseBarrelBlock;
import org.betterx.bclib.blocks.BaseChestBlock;
import org.betterx.bclib.blocks.BaseFurnaceBlock;
import org.betterx.bclib.interfaces.Fuel;
import org.betterx.bclib.interfaces.tools.*;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.wover.block.api.BlockRegistry;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.WoverTagProvider;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;
import org.betterx.wover.tag.api.predefined.MineableTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.registry.FuelRegistry;

public class BlockTagProvider extends WoverTagProvider.ForBlocks {
    public BlockTagProvider(ModCore modCore) {
        super(modCore);
    }

    private static void processBlockCommon(TagBootstrapContext<Block> context, Block block) {
        if (block instanceof BaseChestBlock) {
            BaseBlockEntities.CHEST.registerBlock(block);
        } else if (block instanceof BaseBarrelBlock) {
            BaseBlockEntities.BARREL.registerBlock(block);
        } else if (block instanceof BaseFurnaceBlock) {
            BaseBlockEntities.FURNACE.registerBlock(block);
        }
        if (!(block instanceof PreventMineableAdd)) {
            if (block instanceof AddMineableShears) {
                context.add(block, MineableTags.SHEARS);
            }
            if (block instanceof AddMineableAxe) {
//                if (!context.contains(BlockTags.WOODEN_DOORS, block)
//                        && !context.contains(BlockTags.WOODEN_BUTTONS, block)
//                        && !context.contains(BlockTags.WOODEN_SLABS, block)
//                        && !context.contains(BlockTags.WOODEN_FENCES, block)
//                        && !context.contains(BlockTags.WOODEN_STAIRS, block)
//                        && !context.contains(BlockTags.WOODEN_PRESSURE_PLATES, block)
//                        && !context.contains(BlockTags.WOODEN_TRAPDOORS, block)
//                        && !context.contains(CommonBlockTags.WOODEN_BARREL, block)
//                        && !context.contains(CommonBlockTags.WOODEN_CHEST, block)
//                        && !context.contains(CommonBlockTags.WOODEN_COMPOSTER, block)
//                        && !context.contains(CommonBlockTags.WORKBENCHES, block)
//                        && !context.contains(BlockTags.SIGNS, block)
//                        && !context.contains(BlockTags.PLANKS, block)
//                        && !context.contains(BlockTags.LOGS, block)
//                        && !context.contains(BlockTags.FENCE_GATES, block)
//                        && !context.contains(BlockTags.ALL_HANGING_SIGNS, block)
//                        && !context.contains(CommonBlockTags.WORKBENCHES, block)
//                        && !context.contains(org.betterx.wover.tag.api.predefined.CommonBlockTags.BOOKSHELVES, block)
//                ) {
                context.add(block, MineableTags.AXE);
//                }
            }
            if (block instanceof AddMineablePickaxe) {
                context.add(block, MineableTags.PICKAXE);
            }
            if (block instanceof AddMineableShovel) {
                context.add(block, MineableTags.SHOVEL);
            }
            if (block instanceof AddMineableHoe) {
                context.add(block, MineableTags.HOE);
            }
            if (block instanceof AddMineableSword) {
                context.add(block, MineableTags.SWORD);
            }
            if (block instanceof AddMineableHammer) {
                context.add(block, MineableTags.HAMMER);
            }
        }

        if (block instanceof BehaviourWaterPlantLike) {
            context.add(block, CommonBlockTags.WATER_PLANT);
        }

        if (block instanceof BehaviourPlant || block instanceof BehaviourShearablePlant) {
            context.add(block, CommonBlockTags.PLANT);
        }

        if (block instanceof BehaviourSeedLike) {
            context.add(block, CommonBlockTags.SEEDS);
        }

        if (block instanceof BehaviourSaplingLike) {
            context.add(block, CommonBlockTags.SAPLINGS, BlockTags.SAPLINGS);
        }

        if (block instanceof BehaviourClimable c) {
            context.add(block, BlockTags.CLIMBABLE);
        }

        if (block instanceof BehaviourLeaves) {
            context.add(block, BlockTags.LEAVES, CommonBlockTags.LEAVES);
        }

        if (block instanceof BehaviourImmobile) {
            context.add(block, CommonBlockTags.IMMOBILE);
        }

        if (block instanceof BehaviourObsidian) {
            context.add(block, CommonBlockTags.IS_OBSIDIAN);
        }

        if (block instanceof BehaviourPortalFrame) {
            context.add(block, CommonBlockTags.NETHER_PORTAL_FRAME);
        }

        if (block instanceof BehaviourOre) {
            context.add(block, CommonBlockTags.ORES);
        }

        if (block instanceof Fuel fl) {
            FuelRegistry.INSTANCE.add(block, fl.getFuelTime());
        }

        final ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);
        if (!location.getNamespace().equals("minecraft")) {
            if (!(block instanceof HasMinableBehaviour) && block.defaultBlockState()
                                                                .requiresCorrectToolForDrops()) {
                BCLib.LOGGER.error("Block " + block + "(" + block.getClass() + ")" + " has no mineable behaviour!");
            }
        }
    }

    @Override
    protected void prepareTags(TagBootstrapContext<Block> context) {
        BlockRegistry
                .forMod(modCore)
                .allBlocks()
                .forEach(block -> processBlockCommon(context, block));

        context.add(BCLBlockTags.BONEMEAL_SOURCE_NETHERRACK, Blocks.WARPED_NYLIUM, Blocks.CRIMSON_NYLIUM);
        context.add(BCLBlockTags.BONEMEAL_TARGET_NETHERRACK, Blocks.NETHERRACK);
        context.add(BCLBlockTags.BONEMEAL_TARGET_END_STONE, Blocks.END_STONE);
        context.add(BCLBlockTags.BONEMEAL_TARGET_OBSIDIAN, Blocks.OBSIDIAN);
    }
}
