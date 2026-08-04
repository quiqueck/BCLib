package org.betterx.bclib.blocks;

import org.betterx.bclib.trait.block.SurvivesOnBlockTrait;
import org.betterx.bclib.trait.block.SurvivesOnSolidTrait;
import org.betterx.bclib.util.BlocksHelper;
import de.ambertation.wover.block.api.trait.BlockTrait;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

/**
 * Base for single-block plants.
 * <p>
 * Deliberately does not declare a loot table. The block's owner attaches one at registration with
 * {@code BlockTraits.LOOT_TABLE} (historically this class implemented wover's deprecated
 * {@code BlockLootProvider} and generated {@code dropWithSilkTouchOrShears} for every subclass, which
 * double-generated the table for any block that also carried the trait - the two datagen providers run
 * independently, with no filter between them).
 */
public class BasePlantBlock extends BaseBlockNotFull implements BonemealableBlock {
    private static final VoxelShape SHAPE = box(4, 0, 4, 12, 14, 12);

    public BasePlantBlock(Properties settings) {
        super(settings);
    }

    /**
     * Whether {@code state} (the block below) is valid ground for this plant. Defaults to the block's
     * {@link SurvivesOnBlockTrait} - the same runtime check {@code VegetationBlockMixin} performs for
     * vanilla-bush-based plants - so a plain {@code BasePlantBlock} registered with a survival trait
     * survives on exactly those blocks. "Any solid block" is not expressible as a state test - it needs the
     * level and position - so it is not asked of this method; that rule rides on a
     * {@link SurvivesOnSolidTrait} and is applied by {@link #canSurvive} instead. Never consulted at
     * construction time, so it is order-safe with respect to traits.
     */
    protected boolean isTerrain(BlockState state) {
        return SurvivesOnBlockTrait.survivesOn(this, state);
    }


    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        Vec3 vec3d = state.getOffset(pos);
        return SHAPE.move(vec3d.x, vec3d.y, vec3d.z);
    }

    /**
     * Ground below has to satisfy {@link #isTerrain} - or, for a plant registered with a
     * {@link SurvivesOnSolidTrait}, simply be something a decoration can stand on. The second half is what
     * {@code VegetationBlockMixin} does for vanilla-bush-based plants; this class is a plain {@code Block},
     * so it never reaches that mixin and applies the marker itself.
     */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos downPos = pos.below();
        BlockState down = level.getBlockState(downPos);
        return isTerrain(down)
                || (BlockTrait.hasRuntimeTrait(this, SurvivesOnSolidTrait.KEY)
                        && BlocksHelper.isDecorationSupport(level, downPos, down, Direction.UP));
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
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        ItemEntity item = new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                new ItemStack(this)
        );
        level.addFreshEntity(item);
    }
}
