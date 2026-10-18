package org.betterx.bclib.blocks;

import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class BaseWallPlantBlock extends BasePlantBlock {
    private static final Map<Direction, VoxelShape> DEFAULT_SHAPES = createShapes(1, 8);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    private final Map<Direction, VoxelShape> shapes;

    public BaseWallPlantBlock(Properties settings) {
        this(settings, DEFAULT_SHAPES);
    }

    /**
     * @param shapes the outline per {@link #FACING}, see {@link #createShapes(int, int)}
     */
    public BaseWallPlantBlock(Properties settings, Map<Direction, VoxelShape> shapes) {
        super(settings.offsetType(OffsetType.NONE));
        this.shapes = shapes;
    }

    /**
     * Outlines for a plant attached to the wall behind it.
     *
     * @param inset gap (in pixels) between the outline and the block's top, bottom and sides
     * @param depth how far (in pixels) the outline sticks out of the wall
     * @return the outline per {@link #FACING}
     */
    public static Map<Direction, VoxelShape> createShapes(int inset, int depth) {
        final int max = 16 - inset;
        return Maps.newEnumMap(ImmutableMap.of(
                Direction.NORTH, box(inset, inset, 16 - depth, max, max, 16),
                Direction.SOUTH, box(inset, inset, 0, max, max, depth),
                Direction.WEST, box(16 - depth, inset, inset, 16, max, max),
                Direction.EAST, box(0, inset, inset, depth, max, max)
        ));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction.getOpposite());
        BlockState blockState = level.getBlockState(blockPos);
        return isSupport(level, blockPos, blockState, direction);
    }

    public boolean isSupport(LevelReader world, BlockPos pos, BlockState blockState, Direction direction) {
        return BlocksHelper.isDecorationSupport(world, pos, blockState, direction);
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
    protected @NotNull BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction neighborDirection,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource randomSource
    ) {
        if (!canSurvive(state, level, pos)) {
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
