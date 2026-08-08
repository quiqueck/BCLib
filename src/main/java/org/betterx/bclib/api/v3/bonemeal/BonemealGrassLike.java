package org.betterx.bclib.api.v3.bonemeal;

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
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import de.ambertation.wover.feature.impl.random.RandomPatchFeature;

import java.util.List;

public interface BonemealGrassLike extends BonemealableBlock {
    BlockState getGrowableCoverState(); //Blocks.GRASS.defaultBlockState();
    Block getHostBlock(); //this

    Holder<PlacedFeature> getCoverFeature(); //VegetationPlacements.GRASS_BONEMEAL
    /**
     * The flower features of the surrounding biome.
     * <p>
     * 26.3 collapsed {@code ConfiguredFeature<FC, F>} into {@link Feature}, so this used to be
     * {@code List<ConfiguredFeature<?, ?>>}.
     */
    List<Feature> getFlowerFeatures();  /*serverLevel.getBiome(currentPos)
                                                                    .value()
                                                                    .getGenerationSettings()
                                                                    .getFlowerFeatures();*/

    default boolean canGrowFlower(RandomSource random) {
        return random.nextInt(8) == 0;
    }
    default boolean canGrowCover(RandomSource random) {
        return random.nextInt(10) == 0;
    }

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
            RandomSource random,
            BlockPos pos,
            BlockState state,
            BonemealSource bonemealSource
    ) {
        final BlockPos above = pos.above();
        final BlockState growableState = getGrowableCoverState();

        outerLoop:
        for (int bonemealAttempt = 0; bonemealAttempt < 128; ++bonemealAttempt) {
            BlockPos currentPos = above;

            for (int j = 0; j < bonemealAttempt / 16; ++j) {
                currentPos = currentPos.offset(
                        random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1
                );
                if (!serverLevel.getBlockState(currentPos.below()).is(getHostBlock())
                        || serverLevel.getBlockState(currentPos)
                                      .isCollisionShapeFullBlock(serverLevel, currentPos)) {
                    continue outerLoop;
                }
            }

            BlockState currentState = serverLevel.getBlockState(currentPos);
            if (currentState.is(growableState.getBlock()) && canGrowCover(random)) {
                ((BonemealableBlock) growableState.getBlock()).performBonemeal(
                        serverLevel,
                        random,
                        currentPos,
                        currentState,
                        bonemealSource
                );
            }

            if (currentState.isAir()) {
                Holder<PlacedFeature> boneFeature;
                if (canGrowFlower(random)) {
                    List<Feature> list = getFlowerFeatures();
                    if (list.isEmpty()) {
                        continue;
                    }

                    boneFeature = ((RandomPatchFeature) list.get(0)).feature();
                } else {
                    boneFeature = getCoverFeature();
                }

                boneFeature.value()
                           .place(serverLevel, serverLevel.getChunkSource().getGenerator(), random, currentPos);
            }
        }

    }
}
