package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourWaterPlant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public abstract class BaseUnderwaterWallPlantBlock extends BaseWallPlantBlock implements LiquidBlockContainer, BehaviourWaterPlant {
    public BaseUnderwaterWallPlantBlock(Properties settings) {
        super(settings);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getFluidState(pos).getType() == Fluids.WATER && super.canSurvive(state, level, pos);
    }
}
