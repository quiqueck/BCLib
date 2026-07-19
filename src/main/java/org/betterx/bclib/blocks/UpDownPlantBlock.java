package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

/**
 * Base for plants that grow as a vertical column.
 * <p>
 * Deliberately does not declare a loot table. The block's owner attaches one at registration with
 * {@code BlockTraits.LOOT_TABLE} (historically this class implemented wover's deprecated
 * {@code BlockLootProvider} and generated {@code dropWithSilkTouch} for every subclass, which
 * double-generated the table for any block that also carried the trait - the two datagen providers run
 * independently, with no filter between them).
 */
public abstract class UpDownPlantBlock extends BaseBlockNotFull {
    private static final VoxelShape SHAPE = box(4, 0, 4, 12, 16, 12);

    public UpDownPlantBlock() {
        this(BehaviourBuilders
                .createPlant()
                .sound(SoundType.GRASS)
        );
    }

    public UpDownPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected abstract boolean isTerrain(BlockState state);

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState down = world.getBlockState(pos.below());
        BlockState up = world.getBlockState(pos.above());
        return (isTerrain(down) || down.getBlock() == this) && (isSupport(up, world, pos) || up.getBlock() == this);
    }

    protected boolean isSupport(BlockState state, LevelReader world, BlockPos pos) {
        return canSupportCenter(world, pos.above(), Direction.UP);
    }

    @Override
    @SuppressWarnings("deprecation")
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
    public void playerDestroy(
            Level world,
            Player player,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity,
            ItemStack stack
    ) {
        super.playerDestroy(world, player, pos, state, blockEntity, stack);
        world.updateNeighborsAt(pos.below(), Blocks.AIR);
    }
}
