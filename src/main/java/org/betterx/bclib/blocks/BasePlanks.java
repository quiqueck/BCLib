package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.interfaces.TagProvider;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public abstract class BasePlanks extends BaseBlock implements TagProvider {
    /**
     * Creates a new Block with the passed properties
     *
     * @param settings The properties of the Block.
     */
    protected BasePlanks(Properties settings) {
        super(settings);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.PLANKS);
        itemTags.add(ItemTags.PLANKS);
    }

    public static class Wood extends BasePlanks implements BehaviourWood {
        /**
         * Creates a new Block with the passed properties
         *
         * @param settings The properties of the Block.
         */
        public Wood(Properties settings) {
            super(settings);
        }
    }
}
