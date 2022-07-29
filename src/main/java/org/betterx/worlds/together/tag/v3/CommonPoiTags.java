package org.betterx.worlds.together.tag.v3;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CommonPoiTags {
    public static final TagKey<Block> FISHERMAN_WORKSTATION = TagManager.BLOCKS.makeCommonTag("workstation/fisherman");
    public static final TagKey<Block> FARMER_WORKSTATION = TagManager.BLOCKS.makeCommonTag("workstation/farmer");

    static {
        TagManager.BLOCKS.addOtherTags(FISHERMAN_WORKSTATION, CommonBlockTags.BARREL, CommonBlockTags.WOODEN_BARREL);
        TagManager.BLOCKS.add(FARMER_WORKSTATION, Blocks.COMPOSTER);
    }
}
