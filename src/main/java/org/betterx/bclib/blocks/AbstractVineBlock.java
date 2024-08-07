package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourVine;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractVineBlock extends BaseBlockNotFull implements RenderLayerProvider, BonemealableBlock, BehaviourVine, BlockLootProvider, BlockModelProvider {
    private static final VoxelShape VOXEL_SHAPE = box(2, 0, 2, 14, 16, 14);
    protected final int maxGrowLength;
    protected final int spaceBeneath;
    protected final int growChance;

    private static BlockBehaviour.Properties makeProps(BlockBehaviour.Properties properties, int growChance) {
        if (growChance > 0) return properties.randomTicks();

        return properties;
    }

    protected AbstractVineBlock(
            BlockBehaviour.Properties properties,
            int maxGrowLength,
            int spaceBeneath,
            int growChance
    ) {
        super(makeProps(properties, growChance));
        this.spaceBeneath = Math.max(0, spaceBeneath);
        this.maxGrowLength = Math.max(1, maxGrowLength);
        this.growChance = growChance;
    }

    @Override
    protected abstract void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder);
    protected abstract BlockState makeBottomState(BlockState state);
    protected abstract BlockState makeMiddleState(BlockState state);
    protected abstract BlockState makeTopState(BlockState state);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        Vec3 vec3d = state.getOffset(view, pos);
        return VOXEL_SHAPE.move(vec3d.x, vec3d.y, vec3d.z);
    }

    public boolean canGenerate(BlockState state, LevelReader world, BlockPos pos) {
        return isSupport(state, world, pos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return isSupport(state, world, pos);
    }

    protected boolean isSupport(BlockState state, LevelReader world, BlockPos pos) {
        BlockState up = world.getBlockState(pos.above());
        return up.is(this) || up.is(BlockTags.LEAVES) || canSupportCenter(world, pos.above(), Direction.DOWN);
    }

    @Override
    final public BlockState updateShape(
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
            if (world.getBlockState(pos.below()).getBlock() != this) return makeBottomState(state);
            else if (world.getBlockState(pos.above()).getBlock() != this) return makeTopState(state);
            return makeMiddleState(state);
        }
    }


    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return canGrow(level, pos, null);
    }

    protected boolean canGrow(LevelReader level, BlockPos pos, @Nullable BlockPos bottomOrNull) {
        BlockPos bottom = bottomOrNull == null ? pos : bottomOrNull;
        while (bottomOrNull == null && level.getBlockState(bottom).getBlock() == this) {
            bottom = bottom.below();
        }

        for (int i = 0; i <= spaceBeneath; i++) {
            if (!level.getBlockState(bottom.below(i)).isAir()) {
                return false;
            }
        }

        BlockPos top = pos;
        while (level.getBlockState(top).getBlock() == this) {
            top = top.above();
        }


        return top.getY() - bottom.getY() <= maxGrowLength;
    }

    protected void grow(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos.above()).getBlock() != this) {
            BlocksHelper.setWithoutUpdate(level, pos, makeTopState(defaultBlockState()));
        } else {
            BlocksHelper.setWithoutUpdate(level, pos, makeMiddleState(defaultBlockState()));
        }

        while (level.getBlockState(pos).getBlock() == this) {
            pos = pos.below();
        }
        BlocksHelper.setWithUpdate(level, pos, defaultBlockState());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return canGrow(level, pos, null);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, pos);
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropWithSilkTouchOrHoeOrShears(this);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createCubeModel(this);
        generator.createFlatItem(this);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);

        if (growChance > 0 && random.nextInt(growChance) == 0) {
            if (canGrow(level, pos, pos.below())) {
                grow(level, pos);
            }
        }
    }
}
