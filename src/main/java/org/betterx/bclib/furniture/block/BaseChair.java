package org.betterx.bclib.furniture.block;

import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class BaseChair extends AbstractChair {
    private static final VoxelShape SHAPE_BOTTOM = box(3, 0, 3, 13, 16, 13);
    private static final VoxelShape SHAPE_TOP = box(3, 0, 3, 13, 6, 13);
    private static final VoxelShape COLLIDER = box(3, 0, 3, 13, 10, 13);
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public final Block clothMaterial;

    public BaseChair(Block baseMaterial, Block clothMaterial) {
        super(baseMaterial, 10);
        this.clothMaterial = Objects.requireNonNull(clothMaterial, "Chair cloth material cannot be null (" + baseMaterial.getDescriptionId() + ")");
        this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TOP, false));
    }

    @Deprecated(forRemoval = true)
    public BaseChair(Block baseMaterial) {
        this(baseMaterial, Blocks.RED_WOOL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING, TOP);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return state.getValue(TOP) ? SHAPE_TOP : SHAPE_BOTTOM;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter view,
            BlockPos pos,
            CollisionContext ePos
    ) {
        return state.getValue(TOP) ? Shapes.empty() : COLLIDER;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (state.getValue(TOP))
            return true;
        BlockState up = world.getBlockState(pos.above());
        return up.isAir() || (up.getBlock() == this && up.getValue(TOP));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide())
            BlocksHelper.setWithUpdate(world, pos.above(), state.setValue(TOP, true));
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
        if (state.getValue(TOP)) {
            return world.getBlockState(pos.below()).getBlock() == this ? state : Blocks.AIR.defaultBlockState();
        } else {
            return world.getBlockState(pos.above()).getBlock() == this ? state : Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (state.getValue(TOP)) {
            pos = pos.below();
            state = world.getBlockState(pos);
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (player.isCreative() && state.getValue(TOP) && world.getBlockState(pos.below()).getBlock() == this) {
            world.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    public static class Wood extends BaseChair implements BehaviourWood {
        @Deprecated(forRemoval = true)
        public Wood(Block baseMaterial) {
            super(baseMaterial, Blocks.RED_WOOL);
        }

        public Wood(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }
    }

    public static class Stone extends BaseChair implements BehaviourStone {
        @Deprecated(forRemoval = true)
        public Stone(Block baseMaterial) {
            super(baseMaterial, Blocks.RED_WOOL);
        }

        public Stone(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }
    }

    public static class Metal extends BaseChair implements BehaviourMetal {
        @Deprecated(forRemoval = true)
        public Metal(Block baseMaterial) {
            super(baseMaterial, Blocks.RED_WOOL);
        }

        public Metal(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }
    }

    @Deprecated(forRemoval = true)
    public static BaseChair from(Block source) {
        return BehaviourHelper.from(source, Wood::new, Stone::new, Metal::new);
    }

    public static BaseChair from(Block baseMaterial, Block clothMaterial) {
        return BehaviourHelper.from(baseMaterial, (b) -> new BaseChair.Wood(b, clothMaterial), (b) -> new BaseChair.Stone(b, clothMaterial), (b) -> new BaseChair.Metal(b, clothMaterial));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        BCLModels.createChairBlockModel(generator, this, this.baseMaterial, this.clothMaterial);
    }
}
