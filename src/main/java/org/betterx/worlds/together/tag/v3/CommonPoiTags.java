package org.betterx.worlds.together.tag.v3;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class CommonPoiTags {
    public static final TagKey<Block> FISHERMAN_WORKSTATION = TagManager.BLOCKS.makeCommonTag("workstation/fisherman");

    static {
        TagManager.BLOCKS.addOtherTags(FISHERMAN_WORKSTATION, CommonBlockTags.BARREL, CommonBlockTags.WOODEN_BARREL);
    }
}
