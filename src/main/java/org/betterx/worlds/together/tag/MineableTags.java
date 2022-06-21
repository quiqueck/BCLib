package org.betterx.worlds.together.tag;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;


public class MineableTags {
    public static final TagKey<Block> AXE = BlockTags.MINEABLE_WITH_AXE;
    public static final TagKey<Block> HOE = BlockTags.MINEABLE_WITH_HOE;
    public static final TagKey<Block> PICKAXE = BlockTags.MINEABLE_WITH_PICKAXE;
    public static final TagKey<Block> SHEARS = FabricMineableTags.SHEARS_MINEABLE;
    public static final TagKey<Block> SHOVEL = BlockTags.MINEABLE_WITH_SHOVEL;
    public static final TagKey<Block> SWORD = FabricMineableTags.SWORD_MINEABLE;
    public static final TagKey<Block> HAMMER = TagManager.BLOCKS.makeCommonTag("mineable/hammer");

    static void prepareTags() {
    }
}
