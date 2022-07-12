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

    static void prepareTags() {
    }
}
