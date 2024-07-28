package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.interfaces.tools.AxeCanStrip;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;


public abstract class BaseStripableBarkBlock extends BaseBarkBlock implements AxeCanStrip {
    private final Block strippedBlock;

    protected BaseStripableBarkBlock(Block strippedBlock, Properties settings) {
        super(settings);
        this.strippedBlock = strippedBlock;
    }


    @Override
    public BlockState strippedState(BlockState state) {
        return strippedBlock.defaultBlockState();
    }

    public static class Wood extends BaseStripableBarkBlock implements BehaviourWood, BlockTagProvider, ItemTagProvider {
        private final boolean flammable;

        public Wood(MapColor color, Block strippedBlock, boolean flammable) {
            super(
                    strippedBlock,
                    (flammable
                            ? Properties.ofFullCopy(strippedBlock).ignitedByLava()
                            : Properties.ofFullCopy(strippedBlock)).mapColor(color)
            );
            this.flammable = flammable;
        }

        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, BlockTags.LOGS);
            if (flammable) {
                context.add(this, BlockTags.LOGS_THAT_BURN);
            }
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(this, ItemTags.LOGS);
            if (flammable) {
                context.add(this, ItemTags.LOGS_THAT_BURN);
            }
        }
    }
}
