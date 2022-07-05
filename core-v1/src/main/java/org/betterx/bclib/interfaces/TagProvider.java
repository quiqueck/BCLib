package org.betterx.bclib.interfaces;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface TagProvider {
    void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags);
}
