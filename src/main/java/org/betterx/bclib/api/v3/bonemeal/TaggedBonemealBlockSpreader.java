package org.betterx.bclib.api.v3.bonemeal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

class TaggedBonemealBlockSpreader extends BlockSpreader {
    public final TagKey<Block> blockTag;

    public TaggedBonemealBlockSpreader(TagKey<Block> blockTag) {
        this.blockTag = blockTag;
    }

    @Override
    public boolean canSpreadAt(BlockGetter blockGetter, BlockPos blockPos) {
        final BlockState aboveState = blockGetter.getBlockState(blockPos.above());
        return aboveState.isAir() && aboveState.propagatesSkylightDown(blockGetter, blockPos);
    }

    @Override
    protected boolean isValidSource(BlockState state) {
        return state.is(blockTag);
    }

    @Override
    protected boolean hasCustomBehaviour(BlockState state) {
        return true;
    }
}
