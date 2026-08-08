package org.betterx.bclib.api.v3.bonemeal;

import de.ambertation.wover.feature.api.FeatureUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

import org.jetbrains.annotations.Nullable;

//adapted from NyliumBlock
public interface BonemealNyliumLike extends BonemealableBlock {
    Block getHostBlock(); //this

    /**
     * The feature that is placed on top of the host block.
     * <p>
     * 26.3 collapsed {@code ConfiguredFeature<FC, F>} into {@link Feature} (the whole
     * {@code ...feature.configurations} package is gone and {@link Feature} now carries its own
     * configuration), so this used to be
     * {@code Holder<? extends ConfiguredFeature<?, ? extends Feature<?>>>}.
     */
    @Nullable
    Holder<Feature> getCoverFeature();

    default boolean isValidBonemealTarget(
            LevelReader blockGetter,
            BlockPos blockPos,
            BlockState blockState,
            BonemealSource bonemealSource
    ) {
        return blockGetter.getBlockState(blockPos.above()).isAir();
    }

    default boolean isBonemealSuccess(
            Level level,
            RandomSource randomSource,
            BlockPos blockPos,
            BlockState blockState,
            BonemealSource bonemealSource
    ) {
        return true;
    }

    default void performBonemeal(
            ServerLevel serverLevel,
            RandomSource randomSource,
            BlockPos blockPos,
            BlockState blockState,
            BonemealSource bonemealSource
    ) {
        final BlockState currentState = serverLevel.getBlockState(blockPos);
        if (currentState.is(getHostBlock())) {
            Holder<Feature> feature = getCoverFeature();
            if (feature != null) {
                // unchanged=true: the cover feature is a patch (wover:random_patch or
                // minecraft:nether_forest_vegetation) and has to be placed exactly as authored, so its
                // tries/spread scatter plants over the surrounding surface the way vanilla nylium does.
                // With unchanged=false, FeatureUtils unwraps a RandomPatchConfiguration down to the single
                // block feature inside it (that unwrap exists to find a GrowableFeature, e.g. a sapling's
                // tree) and then places just that one block at blockPos.above() - which made bone meal look
                // like it only ever grew a plant on the block that was clicked.
                FeatureUtils.placeInWorld(feature.value(), serverLevel, blockPos.above(), randomSource, true);
            }
        }
    }
}
