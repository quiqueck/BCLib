package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.items.tool.BaseShearsItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Function;

public abstract class UnderwaterPlantBlock extends BaseBlockNotFull implements RenderLayerProvider, BonemealableBlock, LiquidBlockContainer {
    public static Properties baseUnderwaterPlantSettings() {
        return baseUnderwaterPlantSettings(false, 0);
    }

    public static Properties baseUnderwaterPlantSettings(int light) {
        return baseUnderwaterPlantSettings(false, light);
    }

    public static Properties baseUnderwaterPlantSettings(boolean replaceable) {
        return baseUnderwaterPlantSettings(replaceable, 0);
    }

    public static Properties baseUnderwaterPlantSettings(boolean replaceable, int light) {
        return baseUnderwaterPlantSettings(
                replaceable ? BehaviourBuilders.createReplaceableWaterPlant() : BehaviourBuilders.createWaterPlant(),
                light
        );
    }

    public static Properties baseUnderwaterPlantSettings(BlockBehaviour.Properties props, int light) {
        props
                .sound(SoundType.WET_GRASS)
                .noCollission()
                .offsetType(BlockBehaviour.OffsetType.XZ);
        if (light > 0) props.lightLevel(s -> light);
        return props;
    }

    private static final VoxelShape SHAPE = box(4, 0, 4, 12, 14, 12);

    public UnderwaterPlantBlock() {
        this(p -> p);
    }

    @Deprecated(forRemoval = true)
    public UnderwaterPlantBlock(Function<Properties, Properties> propMod) {
        this(
                propMod.apply(baseUnderwaterPlantSettings())
        );
    }

    public UnderwaterPlantBlock(int light) {
        this(light, p -> p);
    }

    @Deprecated(forRemoval = true)
    public UnderwaterPlantBlock(int light, Function<Properties, Properties> propMod) {
        this(
                propMod.apply(baseUnderwaterPlantSettings(light))
        );
    }

    public UnderwaterPlantBlock(Properties settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        Vec3 vec3d = state.getOffset(view, pos);
        return SHAPE.move(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState down = world.getBlockState(pos.below());
        state = world.getBlockState(pos);
        return isTerrain(down) && state.getFluidState().getType().equals(Fluids.WATER.getSource());
    }

    protected abstract boolean isTerrain(BlockState state);

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(
            BlockState state,
            Direction facing,
            BlockState neighborState,
            LevelAccessor world,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (!canSurvive(state, world, pos)) {
            world.destroyBlock(pos, true);
            return Blocks.WATER.defaultBlockState();
        } else {
            return state;
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (tool != null && BaseShearsItem.isShear(tool) || EnchantmentHelper.getItemEnchantmentLevel(
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

    @Override
    public boolean canPlaceLiquid(BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

}
