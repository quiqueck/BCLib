package org.betterx.bclib.api.v2.tag;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;


public class NamedBlockTags {
    public static final TagKey<Block> ANVIL = BlockTags.ANVIL;
    public static final TagKey<Block> BUTTONS = BlockTags.BUTTONS;
    public static final TagKey<Block> CLIMBABLE = BlockTags.CLIMBABLE;
    public static final TagKey<Block> DOORS = BlockTags.DOORS;
    public static final TagKey<Block> FENCES = BlockTags.FENCES;
    public static final TagKey<Block> FENCE_GATES = BlockTags.FENCE_GATES;
    public static final TagKey<Block> LEAVES = BlockTags.LEAVES;
    public static final TagKey<Block> LOGS = BlockTags.LOGS;
    public static final TagKey<Block> LOGS_THAT_BURN = BlockTags.LOGS_THAT_BURN;
    public static final TagKey<Block> NYLIUM = BlockTags.NYLIUM;
    public static final TagKey<Block> PLANKS = BlockTags.PLANKS;
    public static final TagKey<Block> PRESSURE_PLATES = BlockTags.PRESSURE_PLATES;
    public static final TagKey<Block> SAPLINGS = BlockTags.SAPLINGS;
    public static final TagKey<Block> SIGNS = BlockTags.SIGNS;
    public static final TagKey<Block> SLABS = BlockTags.SLABS;
    public static final TagKey<Block> STAIRS = BlockTags.STAIRS;
    public static final TagKey<Block> STONE_PRESSURE_PLATES = BlockTags.STONE_PRESSURE_PLATES;
    public static final TagKey<Block> TRAPDOORS = BlockTags.TRAPDOORS;
    public static final TagKey<Block> WALLS = BlockTags.WALLS;
    public static final TagKey<Block> WOODEN_BUTTONS = BlockTags.WOODEN_BUTTONS;
    public static final TagKey<Block> WOODEN_DOORS = BlockTags.WOODEN_DOORS;
    public static final TagKey<Block> WOODEN_FENCES = BlockTags.WOODEN_FENCES;
    public static final TagKey<Block> WOODEN_PRESSURE_PLATES = BlockTags.WOODEN_PRESSURE_PLATES;
    public static final TagKey<Block> WOODEN_SLABS = BlockTags.WOODEN_SLABS;
    public static final TagKey<Block> WOODEN_STAIRS = BlockTags.WOODEN_STAIRS;
    public static final TagKey<Block> WOODEN_TRAPDOORS = BlockTags.WOODEN_TRAPDOORS;
    public static final TagKey<Block> SOUL_FIRE_BASE_BLOCKS = BlockTags.SOUL_FIRE_BASE_BLOCKS;
    public static final TagKey<Block> SOUL_SPEED_BLOCKS = BlockTags.SOUL_SPEED_BLOCKS;
    public static final TagKey<Block> BEACON_BASE_BLOCKS = BlockTags.BEACON_BASE_BLOCKS;
    public static final TagKey<Block> STONE_BRICKS = BlockTags.STONE_BRICKS;

    static {
        TagAPI.BLOCKS.add(BlockTags.NETHER_CARVER_REPLACEABLES, Blocks.RED_SAND, Blocks.MAGMA_BLOCK, Blocks.SCULK);
        TagAPI.BLOCKS.addOtherTags(
                BlockTags.NETHER_CARVER_REPLACEABLES,
                CommonBlockTags.NETHER_STONES,
                CommonBlockTags.NETHERRACK
        );
    }
}
