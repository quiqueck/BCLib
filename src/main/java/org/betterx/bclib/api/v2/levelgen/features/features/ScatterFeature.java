package org.betterx.bclib.api.v2.levelgen.features.features;

import org.betterx.bclib.api.v2.levelgen.features.UserGrowableFeature;
import org.betterx.bclib.api.v2.levelgen.features.config.ScatterFeatureConfig;
import org.betterx.bclib.util.BlocksHelper;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.Optional;

public class ScatterFeature<FC extends ScatterFeatureConfig>
        extends Feature<FC> implements UserGrowableFeature<FC> {

    public ScatterFeature(Codec<FC> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FC> featurePlaceContext) {
        final WorldGenLevel level = featurePlaceContext.level();
        final BlockPos origin = featurePlaceContext.origin();
        final RandomSource random = featurePlaceContext.random();

        ScatterFeatureConfig config = featurePlaceContext.config();
        Optional<Direction> direction = getTipDirection(level, origin, random, config);
        if (direction.isEmpty()) {
            return false;
        }
        BlockPos basePos = origin.relative(direction.get(), -1);


        int i = (int) (random.nextFloat() * (1 + config.maxHeight - config.minHeight) + config.minHeight);
        growCenterPillar(level, origin, basePos, direction.get(), i, config, random);
        return true;
    }


    protected void growCenterPillar(
            LevelAccessor level,
            BlockPos origin,
            BlockPos basePos,
            Direction direction,
            int centerHeight,
            ScatterFeatureConfig config,
            RandomSource random
    ) {
        if (config.isValidBase(level.getBlockState(basePos))) {
            final Direction surfaceDirection = direction.getOpposite();
            BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();
            int adaptedHeight = freeHeight(level, direction, centerHeight, config, origin);
            buildPillarWithBase(level, origin, basePos, direction, adaptedHeight, config, random, false);

            final double distNormalizer = (config.maxSpread * Math.sqrt(2));
            final int tryCount = config.spreadCount.sample(random);
            for (int i = 0; i < tryCount; i++) {
                int x = origin.getX() + (int) (random.nextGaussian() * config.maxSpread);
                int z = origin.getZ() + (int) (random.nextGaussian() * config.maxSpread);
                POS.set(x, basePos.getY(), z);

                if (BlocksHelper.findSurroundingSurface(level, POS, surfaceDirection, 4, config::isValidBase)) {
                    int myHeight = freeHeight(
                            level,
                            direction,
                            centerHeight,
                            config,
                            POS
                    );

                    int dx = x - POS.getX();
                    int dz = z - POS.getZ();
                    float sizeFactor = (1 - (float) (Math.sqrt(dx * dx + dz * dz) / distNormalizer));
                    sizeFactor = (1 - (random.nextFloat() * config.sizeVariation)) * sizeFactor;
                    myHeight = (int) Math.min(Math.max(
                            config.minHeight,
                            config.minHeight + sizeFactor * (myHeight - config.minHeight)
                    ), config.maxHeight);

                    BlockState baseState = level.getBlockState(POS.relative(direction.getOpposite()));
                    if (!config.isValidBase(baseState)) {
                        System.out.println("Starting from " + baseState + " at " + POS.relative(direction.getOpposite()));
                    }
                    buildPillarWithBase(level,
                            POS,
                            POS.relative(direction.getOpposite()),
                            direction,
                            myHeight,
                            config,
                            random, false
                    );
                }
            }
        }
    }

    private int freeHeight(
            LevelAccessor level,
            Direction direction,
            int defaultHeight,
            ScatterFeatureConfig config,
            BlockPos POS
    ) {
        int myHeight;
        if (config.growWhileFree) {
            myHeight = BlocksHelper.blockCount(
                    level,
                    POS,
                    direction,
                    config.maxHeight,
                    BlocksHelper::isFree
            );
        } else {
            myHeight = defaultHeight;
        }
        return Math.max(config.minHeight, myHeight);
    }

    private void buildPillarWithBase(
            LevelAccessor level,
            BlockPos origin,
            BlockPos basePos,
            Direction direction,
            int height,
            ScatterFeatureConfig config,
            RandomSource random,
            boolean force
    ) {
        if (force || BlocksHelper.isFreeSpace(level, origin, direction, height, BlocksHelper::isFree)) {
            createPatchOfBaseBlocks(level, random, basePos, config);
            BlockState bottom = config.bottomBlock.getState(random, origin);
            if (bottom.canSurvive(level, origin)) {
                buildPillar(level, origin, direction, height, config, random);
            }
        }
    }

    private void buildPillar(
            LevelAccessor level,
            BlockPos origin,
            Direction direction,
            int height,
            ScatterFeatureConfig config,
            RandomSource random
    ) {

        final BlockPos.MutableBlockPos POS = origin.mutable();
        for (int size = 0; size < height; size++) {
            BlockState state = config.createBlock(size, height - 1, random, POS);
            BlocksHelper.setWithoutUpdate(level, POS, state);
            POS.move(direction);
        }
    }

    private Optional<Direction> getTipDirection(
            LevelAccessor levelAccessor,
            BlockPos blockPos,
            RandomSource randomSource,
            ScatterFeatureConfig config
    ) {
        boolean onCeil = config.floorChance < 1 && config.isValidBase(levelAccessor.getBlockState(blockPos.above()));
        boolean onFloor = config.floorChance > 0 && config.isValidBase(levelAccessor.getBlockState(blockPos.below()));

        if (onCeil && onFloor) {
            return Optional.of(config.isFloor(randomSource) ? Direction.DOWN : Direction.UP);
        }
        if (onCeil) {
            return Optional.of(Direction.DOWN);
        }
        if (onFloor) {
            return Optional.of(Direction.UP);
        }
        return Optional.empty();
    }

    private void createPatchOfBaseBlocks(
            LevelAccessor levelAccessor,
            RandomSource randomSource,
            BlockPos blockPos,
            ScatterFeatureConfig config
    ) {
        if (config.baseState.isPresent() && config.baseReplaceChance > 0 && randomSource.nextFloat() < config.baseReplaceChance) {
            final BlockState baseState = config.baseState.get();
            BlockPos pos;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (randomSource.nextFloat() > config.chanceOfDirectionalSpread) continue;
                pos = blockPos.relative(direction);
                placeBaseBlockIfPossible(levelAccessor, pos, baseState);

                if (randomSource.nextFloat() > config.chanceOfSpreadRadius2) continue;
                pos = pos.relative(Direction.getRandom(randomSource));
                placeBaseBlockIfPossible(levelAccessor, pos, baseState);

                if (randomSource.nextFloat() > config.chanceOfSpreadRadius3) continue;
                pos = pos.relative(Direction.getRandom(randomSource));
                placeBaseBlockIfPossible(levelAccessor, pos, baseState);
            }
            placeBaseBlockIfPossible(levelAccessor, blockPos, baseState);
        }
    }

    protected void placeBaseBlockIfPossible(
            LevelAccessor levelAccessor,
            BlockPos blockPos,
            BlockState baseState
    ) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (BlocksHelper.isTerrain(blockState)) {
            levelAccessor.setBlock(blockPos, baseState, 2);
        }
    }

    @Override
    public boolean grow(
            ServerLevelAccessor level,
            BlockPos origin,
            RandomSource random,
            FC config
    ) {
        Optional<Direction> oDirection = getTipDirection(level, origin, random, config);
        if (oDirection.isEmpty()) {
            return false;
        }
        Direction direction = oDirection.get();
        BlockPos basePos = origin.relative(direction, -1);

        if (config.isValidBase(level.getBlockState(basePos))) {
            int centerHeight = (int) (random.nextFloat() * (1 + config.maxHeight - config.minHeight) + config.minHeight);
            centerHeight = freeHeight(
                    level,
                    direction,
                    centerHeight,
                    config,
                    origin.relative(direction, 1)
            ) + 1;
            buildPillarWithBase(level, origin, basePos, direction, centerHeight, config, random, true);
        }
        return false;
    }

}
