package org.betterx.bclib.api.v2.poi;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public interface PoiTypeExtension {
    void bcl_setTag(TagKey<Block> tag);
    TagKey<Block> bcl_getTag();
}
