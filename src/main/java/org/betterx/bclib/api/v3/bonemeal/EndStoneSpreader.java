package org.betterx.bclib.api.v3.bonemeal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class EndStoneSpreader implements BonemealSpreader {
    public static final EndStoneSpreader INSTANCE = new EndStoneSpreader();

    @Override
    public boolean isValidBonemealSpreadTarget(
            BlockGetter blockGetter,
            BlockPos blockPos,
            BlockState blockState,
            boolean bl
    ) {
        return false;
    }

    @Override
    public boolean performBonemealSpread(
            ServerLevel serverLevel,
            RandomSource randomSource,
            BlockPos blockPos,
            BlockState blockState
    ) {
        return false;
    }
}
