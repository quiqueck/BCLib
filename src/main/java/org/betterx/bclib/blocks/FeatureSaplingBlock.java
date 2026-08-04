package org.betterx.bclib.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

public class FeatureSaplingBlock<F extends Feature<FC>, FC extends FeatureConfiguration> extends SaplingBlock {

    @FunctionalInterface
    public interface FeatureSupplier<F extends Feature<FC>, FC extends FeatureConfiguration> {
        boolean grow(
                @NotNull ServerLevel level,
                @NotNull BlockPos pos,
                @NotNull BlockState state,
                @NotNull RandomSource random
        );
    }

    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 14, 12);
    private static final VoxelShape HANGING_SHAPE = Block.box(4, 2, 4, 12, 16, 12);
    private final FeatureSupplier<F, FC> feature;
    private final boolean hangsFromAbove;

    public FeatureSaplingBlock(
            BlockBehaviour.Properties properties,
            FeatureSupplier<F, FC> featureSupplier
    ) {
        this(properties, featureSupplier, false);
    }

    /**
     * @param hangsFromAbove when {@code true} the sapling attaches to the block <em>above</em> it
     *                       (ceiling-hung, e.g. anchor-tree / nether-sakura branches) and uses a
     *                       taller upward {@link #getShape}; when {@code false} it behaves as a normal
     *                       ground sapling attaching to the block below. This is a construction-time
     *                       parameter (R8): it selects the shape/attachment, which must be known before
     *                       any world exists.
     */
    public FeatureSaplingBlock(
            BlockBehaviour.Properties properties,
            FeatureSupplier<F, FC> featureSupplier,
            boolean hangsFromAbove
    ) {
        super(null, properties);
        this.feature = featureSupplier;
        this.hangsFromAbove = hangsFromAbove;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        if (hangsFromAbove) {
            final BlockPos target = blockPos.above();
            return this.mayPlaceOn(levelReader.getBlockState(target), levelReader, target);
        }
        return super.canSurvive(blockState, levelReader, blockPos);
    }

    protected boolean growFeature(
            @NotNull ServerLevel world,
            @NotNull BlockPos pos,
            @NotNull BlockState blockState,
            @NotNull RandomSource random
    ) {
        if (feature != null) {
            return feature.grow(world, pos, blockState, random);
        }
        return false;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction neighborDirection,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource randomSource
    ) {
        if (!canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextInt(16) == 0;
    }

    @Override
    public void advanceTree(ServerLevel world, BlockPos pos, BlockState blockState, RandomSource random) {
        if (blockState.getValue(STAGE) == 0) {
            world.setBlock(pos, blockState.cycle(STAGE), 4);
        } else {
            doGrowFeature(world, pos, blockState, random);
        }
    }

    /**
     * Grows the feature right now, skipping the {@code STAGE} step {@link #advanceTree} would spend an
     * application on.
     * <p>
     * {@code performBonemeal} goes through {@code advanceTree}, so a sapling sitting at {@code STAGE 0}
     * takes two applications to become a tree. That second click is the survival pacing; a creative
     * player bypassing the {@link #isBonemealSuccess} roll should not still have to click twice. This is
     * the entry point {@code BoneMealItemMixin} uses for that - {@link #doGrowFeature} itself is
     * {@code protected}, so a mixin in another package cannot reach it.
     *
     * @return {@code true} if the feature placed; {@code false} leaves the sapling untouched, e.g. when
     *         the tree has no room
     */
    public boolean growFeatureNow(
            ServerLevel serverLevel,
            BlockPos blockPos,
            BlockState originalBlockState,
            RandomSource randomSource
    ) {
        return doGrowFeature(serverLevel, blockPos, originalBlockState, randomSource);
    }

    protected boolean doGrowFeature(
            ServerLevel serverLevel,
            BlockPos blockPos,
            BlockState originalBlockState,
            RandomSource randomSource
    ) {
        if (feature == null) {
            return false;
        } else {
            BlockState emptyState = serverLevel.getFluidState(blockPos).createLegacyBlock();
            serverLevel.setBlock(blockPos, emptyState, 4);
            ;
            if (growFeature(serverLevel, blockPos, originalBlockState, randomSource)) {
                if (serverLevel.getBlockState(blockPos) == emptyState) {
                    serverLevel.sendBlockUpdated(blockPos, originalBlockState, emptyState, 2);
                }

                return true;
            } else {
                serverLevel.setBlock(blockPos, originalBlockState, 4);
                return false;
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        this.tick(state, world, pos, random);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        if (isBonemealSuccess(world, random, pos, state)) {
            performBonemeal(world, random, pos, state);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return hangsFromAbove ? HANGING_SHAPE : SHAPE;
    }
}
