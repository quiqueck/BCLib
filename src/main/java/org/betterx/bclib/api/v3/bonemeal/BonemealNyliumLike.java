package org.betterx.bclib.api.v3.bonemeal;

import org.betterx.wover.feature.api.FeatureUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;

import org.jetbrains.annotations.Nullable;

//adapted from NyliumBlock
public interface BonemealNyliumLike extends BonemealableBlock {
    Block getHostBlock(); //this
    @Nullable
    Holder<? extends ConfiguredFeature<?, ? extends Feature<?>>> getCoverFeature();

    default boolean isValidBonemealTarget(
            LevelReader blockGetter,
            BlockPos blockPos,
            BlockState blockState
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
        if (currentState.is(getHostBlock())) {
            Holder<? extends ConfiguredFeature<?, ?>> feature = getCoverFeature();
            if (feature != null) {
                FeatureUtils.placeInWorld(feature.value(), serverLevel, blockPos.above(), randomSource, false);
            }
        }
    }
}
