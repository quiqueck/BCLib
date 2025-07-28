package org.betterx.bclib.blocks;

import org.betterx.wover.block.api.BlockProperties;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


//TODO: If we keep this, needs to be renamed to BaseStalactiteBlock or similar
public class StalactiteBlock extends BaseBlockNotFull implements SimpleWaterloggedBlock, LiquidBlockContainer {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty IS_FLOOR = BlockProperties.IS_FLOOR;
    public static final IntegerProperty SIZE = BlockProperties.SIZE;
    private static final VoxelShape[] SHAPES;

    public StalactiteBlock(Block source) {
        this(Properties.ofFullCopy(source).noOcclusion());
    }

    public StalactiteBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(getStateDefinition()
                .any()
                .setValue(SIZE, 0)
                .setValue(IS_FLOOR, true)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(WATERLOGGED, IS_FLOOR, SIZE);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPES[state.getValue(SIZE)];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        LevelReader world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction dir = ctx.getClickedFace();
        boolean water = world.getFluidState(pos).getType() == Fluids.WATER;

        if (dir == Direction.DOWN) {
            if (isThis(world, pos.above()) || canSupportCenter(world, pos.above(), Direction.DOWN)) {
                return defaultBlockState().setValue(IS_FLOOR, false).setValue(WATERLOGGED, water);
            } else if (isThis(world, pos.below()) || canSupportCenter(world, pos.below(), Direction.UP)) {
                return defaultBlockState().setValue(IS_FLOOR, true).setValue(WATERLOGGED, water);
            } else {
                return null;
            }
        } else {
            if (isThis(world, pos.below()) || canSupportCenter(world, pos.below(), Direction.UP)) {
                return defaultBlockState().setValue(IS_FLOOR, true).setValue(WATERLOGGED, water);
            } else if (isThis(world, pos.above()) || canSupportCenter(world, pos.above(), Direction.DOWN)) {
                return defaultBlockState().setValue(IS_FLOOR, false).setValue(WATERLOGGED, water);
            } else {
                return null;
            }
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        boolean hasUp = isThis(world, pos.above());
        boolean hasDown = isThis(world, pos.below());
        MutableBlockPos mut = new MutableBlockPos();
        if (hasUp && hasDown) {
            boolean floor = state.getValue(IS_FLOOR);
            BlockPos second = floor ? pos.above() : pos.below();
            BlockState bState = world.getBlockState(second);
            world.setBlockAndUpdate(pos, state.setValue(SIZE, 1).setValue(IS_FLOOR, floor));
            world.setBlockAndUpdate(second, bState.setValue(SIZE, 1).setValue(IS_FLOOR, !floor));

            bState = state;
            int startSize = floor ? 1 : 2;
            mut.set(pos.getX(), pos.getY() + 1, pos.getZ());
            for (int i = 0; i < 8 && isThis(bState); i++) {
                world.setBlockAndUpdate(
                        mut,
                        bState.setValue(SIZE, Math.min(7, startSize + i)).setValue(IS_FLOOR, false)
                );
                mut.setY(mut.getY() + 1);
                bState = world.getBlockState(mut);
            }

            bState = state;
            startSize = floor ? 2 : 1;
            mut.set(pos.getX(), pos.getY() - 1, pos.getZ());
            for (int i = 0; i < 8 && isThis(bState); i++) {
                world.setBlockAndUpdate(
                        mut,
                        bState.setValue(SIZE, Math.min(7, startSize + i)).setValue(IS_FLOOR, true)
                );
                mut.setY(mut.getY() - 1);
                bState = world.getBlockState(mut);
            }
        } else if (hasDown) {
            mut.setX(pos.getX());
            mut.setZ(pos.getZ());
            for (int i = 1; i < 8; i++) {
                mut.setY(pos.getY() - i);
                if (isThis(world, mut)) {
                    BlockState state2 = world.getBlockState(mut);
                    int size = state2.getValue(SIZE);
                    if (size < i) {
                        world.setBlockAndUpdate(mut, state2.setValue(SIZE, i).setValue(IS_FLOOR, true));
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        } else if (hasUp) {
            mut.setX(pos.getX());
            mut.setZ(pos.getZ());
            for (int i = 1; i < 8; i++) {
                mut.setY(pos.getY() + i);
                if (isThis(world, mut)) {
                    BlockState state2 = world.getBlockState(mut);
                    int size = state2.getValue(SIZE);
                    if (size < i) {
                        world.setBlockAndUpdate(mut, state2.setValue(SIZE, i).setValue(IS_FLOOR, false));
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    private boolean isThis(LevelReader world, BlockPos pos) {
        return isThis(world.getBlockState(pos));
    }

    private boolean isThis(BlockState state) {
        return state.getBlock() instanceof StalactiteBlock;
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
        }
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        int size = state.getValue(SIZE);
        return checkUp(world, pos, size) || checkDown(world, pos, size);
    }

    private boolean checkUp(BlockGetter world, BlockPos pos, int size) {
        BlockPos p = pos.above();
        BlockState state = world.getBlockState(p);
        return (isThis(state) && state.getValue(SIZE) >= size) || state.isCollisionShapeFullBlock(world, p);
    }

    private boolean checkDown(BlockGetter world, BlockPos pos, int size) {
        BlockPos p = pos.below();
        BlockState state = world.getBlockState(p);
        return (isThis(state) && state.getValue(SIZE) >= size) || state.isCollisionShapeFullBlock(world, p);
    }

    @Override
    public boolean canPlaceLiquid(
            @Nullable LivingEntity livingEntity,
            BlockGetter blockGetter,
            BlockPos blockPos,
            BlockState blockState,
            Fluid fluid
    ) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }


    static {
        float end = 2F / 8F;
        float start = 5F / 8F;
        SHAPES = new VoxelShape[8];
        for (int i = 0; i < 8; i++) {
            int side = Mth.floor(Mth.lerp(i / 7F, start, end) * 8F + 0.5F);
            SHAPES[i] = box(side, 0, side, 16 - side, 16, 16 - side);
        }
    }
}