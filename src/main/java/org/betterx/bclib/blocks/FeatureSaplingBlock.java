package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;

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
import net.minecraft.world.level.block.SoundType;
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
    private final FeatureSupplier<F, FC> feature;

    public FeatureSaplingBlock(FeatureSupplier<F, FC> featureSupplier) {
        this(0, featureSupplier);
    }

    public FeatureSaplingBlock(int light, FeatureSupplier<F, FC> featureSupplier) {
        this(
                BehaviourBuilders.createPlant().randomTicks()
                                 .noCollission()
                                 .lightLevel(state -> light)
                                 .sound(SoundType.GRASS),
                featureSupplier
        );
    }

    public FeatureSaplingBlock(
            BlockBehaviour.Properties properties,
            FeatureSupplier<F, FC> featureSupplier
    ) {
        super(null, properties);
        this.feature = featureSupplier;
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
        return SHAPE;
    }
}
