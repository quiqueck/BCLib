package org.betterx.bclib.api.v3.bonemeal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface BonemealBlockSpreader {
    boolean isValidBonemealSpreadTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl);
    boolean canSpreadAt(BlockGetter blockGetter, BlockPos blockPos);

    boolean performBonemealSpread(
            ServerLevel serverLevel,
            RandomSource randomSource,
            BlockPos blockPos,
            BlockState blockState
    );

}
