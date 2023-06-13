package org.betterx.bclib.blocks;

import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.EnumMap;

public abstract class BaseWallPlantBlock extends BasePlantBlock {
    private static final EnumMap<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, box(1, 1, 8, 15, 15, 16),
            Direction.SOUTH, box(1, 1, 0, 15, 15, 8),
            Direction.WEST, box(8, 1, 1, 16, 15, 15),
            Direction.EAST, box(0, 1, 1, 8, 15, 15)
    ));
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected BaseWallPlantBlock(Properties settings) {
        super(settings.offsetType(OffsetType.NONE));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction.getOpposite());
        BlockState blockState = level.getBlockState(blockPos);
        return isSupport(level, blockPos, blockState, direction);
    }

    public boolean isSupport(LevelReader world, BlockPos pos, BlockState blockState, Direction direction) {
        return blockState.isSolid() && blockState.isFaceSturdy(world, pos, direction);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = this.defaultBlockState();
        LevelReader worldView = ctx.getLevel();
        BlockPos blockPos = ctx.getClickedPos();
        Direction[] directions = ctx.getNearestLookingDirections();
        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction2 = direction.getOpposite();
                blockState = blockState.setValue(FACING, direction2);
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState;
                }
            }
        }
        return null;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction facing,
            BlockState neighborState,
            LevelAccessor world,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (!canSurvive(state, world, pos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            return state;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return BlocksHelper.rotateHorizontal(state, rotation, FACING);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return BlocksHelper.mirrorHorizontal(state, mirror, FACING);
    }
}
