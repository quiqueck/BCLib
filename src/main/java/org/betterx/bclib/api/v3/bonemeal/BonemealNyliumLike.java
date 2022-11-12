package org.betterx.bclib.api.v3.bonemeal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public interface BonemealNyliumLike extends BonemealableBlock {
    Block hostBlock(); //this
    Holder<PlacedFeature> coverFeature();

    default boolean isValidBonemealTarget(
            BlockGetter blockGetter,
            BlockPos blockPos,
            BlockState blockState,
            boolean bl
    ) {
        return blockGetter.getBlockState(blockPos.above()).isAir();
    }

    default boolean isBonemealSuccess(
            Level level,
            RandomSource randomSource,
            BlockPos blockPos,
            BlockState blockState
    ) {
        return true;
    }

    default void performBonemeal(
            ServerLevel serverLevel,
            RandomSource randomSource,
            BlockPos blockPos,
            BlockState blockState
    ) {
        final BlockState currentState = serverLevel.getBlockState(blockPos);
        final BlockPos above = blockPos.above();
        final ChunkGenerator generator = serverLevel.getChunkSource().getGenerator();
        if (currentState.is(hostBlock())) {
            coverFeature()
                    .value()
                    .place(serverLevel, generator, randomSource, above);
        }
    }
}
