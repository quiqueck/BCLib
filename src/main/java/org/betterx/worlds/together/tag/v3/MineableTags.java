package org.betterx.worlds.together.tag.v3;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;


public class MineableTags {
    public static final TagKey<Block> AXE = BlockTags.MINEABLE_WITH_AXE;
    public static final TagKey<Block> HOE = BlockTags.MINEABLE_WITH_HOE;
    public static final TagKey<Block> PICKAXE = BlockTags.MINEABLE_WITH_PICKAXE;
    public static final TagKey<Block> SHEARS = TagManager.BLOCKS.makeTag("fabric", "mineable/shears");
    public static final TagKey<Block> SHOVEL = BlockTags.MINEABLE_WITH_SHOVEL;
    public static final TagKey<Block> SWORD = TagManager.BLOCKS.makeTag("fabric", "mineable/sword");
    public static final TagKey<Block> HAMMER = TagManager.BLOCKS.makeCommonTag("mineable/hammer");

    public static final TagKey<Block> NEEDS_NETHERITE_TOOL = TagManager.BLOCKS.makeCommonTag("needs_netherite_tool");
    public static final TagKey<Block> NEEDS_GOLD_TOOL = TagManager.BLOCKS.makeCommonTag("needs_diamond_tool");

    static void prepareTags() {
        TagManager.BLOCKS.addOtherTags(BlockTags.INCORRECT_FOR_GOLD_TOOL, BlockTags.MINEABLE_WITH_PICKAXE);
    }
}
