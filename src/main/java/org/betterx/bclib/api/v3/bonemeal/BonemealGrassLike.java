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
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public interface BonemealGrassLike extends BonemealableBlock {
    BlockState getGrowableCoverState(); //Blocks.GRASS.defaultBlockState();
    Block getHostBlock(); //this

    Holder<PlacedFeature> getCoverFeature(); //VegetationPlacements.GRASS_BONEMEAL
    List<ConfiguredFeature<?, ?>> getFlowerFeatures();  /*serverLevel.getBiome(currentPos)
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

    default void performBonemeal(ServerLevel serverLevel, RandomSource random, BlockPos pos, BlockState state) {
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
                        currentState
                );
            }

            if (currentState.isAir()) {
                Holder<PlacedFeature> boneFeature;
                if (canGrowFlower(random)) {
                    List<ConfiguredFeature<?, ?>> list = getFlowerFeatures();
                    if (list.isEmpty()) {
                        continue;
                    }

                    boneFeature = ((RandomPatchConfiguration) list.get(0).config()).feature();
                } else {
                    boneFeature = getCoverFeature();
                }

                boneFeature.value()
                           .place(serverLevel, serverLevel.getChunkSource().getGenerator(), random, currentPos);
            }
        }

    }
}
