package org.betterx.bclib.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FeatureHangingSaplingBlock<F extends Feature<FC>, FC extends FeatureConfiguration> extends FeatureSaplingBlock {

    private static final VoxelShape SHAPE = Block.box(4, 2, 4, 12, 16, 12);

    public FeatureHangingSaplingBlock(FeatureSupplier<F, FC> featureSupplier) {
        super(featureSupplier);
    }

    public FeatureHangingSaplingBlock(
            FeatureSupplier<F, FC> featureSupplier,
            int light
    ) {
        super(light, featureSupplier);
    }

    public FeatureHangingSaplingBlock(
            BlockBehaviour.Properties properties,
            FeatureSupplier<F, FC> featureSupplier
    ) {
        super(properties, featureSupplier);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        final BlockPos target = blockPos.above();
        return this.mayPlaceOn(levelReader.getBlockState(target), levelReader, target);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

}
