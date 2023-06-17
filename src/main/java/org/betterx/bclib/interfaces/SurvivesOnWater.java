package org.betterx.bclib.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface SurvivesOnWater extends SurvivesOnBlocks {
    List<Block> BLOCKS = List.of(Blocks.WATER);

    @Override
    default List<Block> getSurvivableBlocks() {
        return BLOCKS;
    }

    default boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos).isAir() && isTerrain(world.getBlockState(pos.below()));
    }
}
