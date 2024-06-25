package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourVine;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.wover.block.api.BlockProperties.TripleShape;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BaseVineBlock extends BaseBlockNotFull implements RenderLayerProvider, BonemealableBlock, BehaviourVine, BlockLootProvider, BlockModelProvider {
    public static final EnumProperty<TripleShape> SHAPE = BlockProperties.TRIPLE_SHAPE;
    private static final VoxelShape VOXEL_SHAPE = box(2, 0, 2, 14, 16, 14);

    public BaseVineBlock() {
        this(0, false);
    }

    public BaseVineBlock(int light) {
        this(light, false);
    }

    public BaseVineBlock(int light, boolean bottomOnly) {
        this(light, bottomOnly, p -> p);
    }

    public BaseVineBlock(int light, boolean bottomOnly, Function<Properties, Properties> propMod) {
        this(
                propMod.apply(BehaviourBuilders
                        .createPlant()
                        .sound(SoundType.GRASS)
                        .lightLevel((state) -> bottomOnly
                                ? state.getValue(SHAPE) == TripleShape.BOTTOM
                                ? light
                                : 0
                                : light)
                        .offsetType(BlockBehaviour.OffsetType.XZ))
        );
    }

    public BaseVineBlock(BlockBehaviour.Properties properties) {
        super(properties.offsetType(BlockBehaviour.OffsetType.XZ));
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, TripleShape.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(SHAPE);
    }

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
            if (world.getBlockState(pos.below()).getBlock() != this) return state.setValue(SHAPE, TripleShape.BOTTOM);
            else if (world.getBlockState(pos.above()).getBlock() != this) return state.setValue(SHAPE, TripleShape.TOP);
            return state.setValue(SHAPE, TripleShape.MIDDLE);
        }
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        while (world.getBlockState(pos).getBlock() == this) {
            pos = pos.below();
        }
        return world.getBlockState(pos).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        while (level.getBlockState(pos).getBlock() == this) {
            pos = pos.below();
        }
        return level.isEmptyBlock(pos);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        while (level.getBlockState(pos).getBlock() == this) {
            pos = pos.below();
        }
        level.setBlockAndUpdate(pos, defaultBlockState());
        BlocksHelper.setWithoutUpdate(level, pos, defaultBlockState());
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropWithSilkTouch(this);
    }

    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createCubeModel(this);
        generator.createFlatItem(this);
    }
}
