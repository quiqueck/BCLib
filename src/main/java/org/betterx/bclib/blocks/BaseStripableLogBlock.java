package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.interfaces.TagProvider;
import org.betterx.bclib.interfaces.tools.AxeCanStrip;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.List;


public abstract class BaseStripableLogBlock extends BaseRotatedPillarBlock implements AxeCanStrip {
    private final Block striped;

    protected BaseStripableLogBlock(Block striped, Properties settings) {
        super(settings);
        this.striped = striped;
    }

    @Override
    public BlockState strippedState(BlockState state) {
        return striped.defaultBlockState();
    }

    public static class Wood extends BaseStripableLogBlock implements BehaviourWood, TagProvider {
        private final boolean flammable;

        public Wood(MapColor color, Block striped, boolean flammable) {
            super(
                    striped,
                    (flammable
                            ? Properties.ofFullCopy(striped).ignitedByLava()
                            : Properties.ofFullCopy(striped)).mapColor(color)
            );
            this.flammable = flammable;
        }

        @Override
        public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
            blockTags.add(BlockTags.LOGS);
            itemTags.add(ItemTags.LOGS);

            if (flammable) {
                blockTags.add(BlockTags.LOGS_THAT_BURN);
                itemTags.add(ItemTags.LOGS_THAT_BURN);
            }
        }
    }
}
