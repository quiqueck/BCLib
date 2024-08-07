package org.betterx.bclib.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BaseSimpleVineBlock extends AbstractVineBlock {
    public static final BooleanProperty BOTTOM = BlockProperties.BOTTOM;

    public BaseSimpleVineBlock(Properties properties, int maxGrowLength, int spaceBeneath) {
        super(properties, maxGrowLength, spaceBeneath, 0);
    }

    private BaseSimpleVineBlock(Properties properties, int maxGrowLength, int spaceBeneath, int growChance) {
        super(properties, maxGrowLength, spaceBeneath, growChance);
        this.registerDefaultState(getStateDefinition().any().setValue(BOTTOM, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BOTTOM);
    }

    @Override
    protected BlockState makeBottomState(BlockState state) {
        return state.setValue(BOTTOM, true);
    }

    @Override
    protected BlockState makeMiddleState(BlockState state) {
        return state.setValue(BOTTOM, false);
    }

    @Override
    protected BlockState makeTopState(BlockState state) {
        return state.setValue(BOTTOM, false);
    }

    public static class Growing extends BaseSimpleVineBlock {
        public Growing(
                Properties properties,
                int maxGrowLength,
                int spaceBeneath,
                int growChance
        ) {
            super(properties.randomTicks(), maxGrowLength, spaceBeneath, growChance);
        }


        @Override
        public boolean isRandomlyTicking(BlockState state) {
            return state.getValue(BOTTOM);
        }
    }
}
