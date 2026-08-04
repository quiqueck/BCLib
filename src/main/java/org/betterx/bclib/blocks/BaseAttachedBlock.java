package org.betterx.bclib.blocks;

import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

@SuppressWarnings("deprecation")
public abstract class BaseAttachedBlock extends BaseBlockNotFull {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    protected BaseAttachedBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = defaultBlockState();
        LevelReader worldView = ctx.getLevel();
        BlockPos blockPos = ctx.getClickedPos();
        Direction[] directions = ctx.getNearestLookingDirections();
        for (Direction direction : directions) {
            Direction direction2 = direction.getOpposite();
            blockState = blockState.setValue(FACING, direction2);
            if (blockState.canSurvive(worldView, blockPos)) {
                return blockState;
            }
        }
        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction.getOpposite());
        if (canSupportCenter(world, blockPos, direction)) return true;

        // Leaves are never a supporting center (LeavesBlock.getBlockSupportShape is empty), so cube leaves
        // need this exemption - but minecraft:leaves also holds thin decoration (FurBlock furs and
        // *_outer_leaves) that only fills the half of its block nearest its own support; attaching to the
        // empty half of one of those leaves this block floating half a block off what it hangs on.
        // NOTE: canSurvive does not gate worldgen - the tree/bush features write with setWithoutUpdate - so
        // this rule only holds for generated content because those features now run
        // EndTreeHelper.pruneUnsupportedFur, which enforces exactly this predicate on what they placed.
        return BlocksHelper.isCubeLeaves(world, blockPos, world.getBlockState(blockPos));
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
        if (!canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            return state;
        }
    }


    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return BlocksHelper.rotateHorizontal(state, rotation, FACING);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return BlocksHelper.mirrorHorizontal(state, mirror, FACING);
    }

    public static class Wood extends BaseAttachedBlock {
        public Wood(Properties settings) {
            super(settings);
        }
    }

    public static class Stone extends BaseAttachedBlock {
        public Stone(Properties settings) {
            super(settings);
        }
    }

    public static class Metal extends BaseAttachedBlock {
        public Metal(Properties settings) {
            super(settings);
        }
    }

    public static class Glass extends BaseAttachedBlock {
        public Glass(Properties settings) {
            super(settings);
        }
    }
}
