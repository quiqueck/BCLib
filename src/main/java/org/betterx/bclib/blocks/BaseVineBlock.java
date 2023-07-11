package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourVine;
import org.betterx.bclib.blocks.BlockProperties.TripleShape;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.LootUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class BaseVineBlock extends BaseBlockNotFull implements RenderLayerProvider, BonemealableBlock, BehaviourVine {
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
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (LootUtil.isCorrectTool(this, state, tool) || EnchantmentHelper.getItemEnchantmentLevel(
                Enchantments.SILK_TOUCH,
                tool
        ) > 0) {
            return Lists.newArrayList(new ItemStack(this));
        } else {
            return Lists.newArrayList();
        }
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, boolean isClient) {
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
}
