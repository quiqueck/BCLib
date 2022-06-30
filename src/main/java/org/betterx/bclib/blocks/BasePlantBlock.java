package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.interfaces.SettingsExtender;
import org.betterx.bclib.items.tool.BaseShearsItem;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.jetbrains.annotations.Nullable;

public abstract class BasePlantBlock extends BaseBlockNotFull implements RenderLayerProvider, BonemealableBlock {
    public static Properties basePlantSettings() {
        return basePlantSettings(false, 0);
    }

    public static Properties basePlantSettings(int light) {
        return basePlantSettings(false, light);
    }

    public static Properties basePlantSettings(boolean replaceable) {
        return basePlantSettings(replaceable, 0);
    }

    public static Properties basePlantSettings(boolean replaceable, int light) {
        return basePlantSettings(replaceable ? Material.REPLACEABLE_PLANT : Material.PLANT, light);
    }

    public static Properties basePlantSettings(Material mat, int light) {
        Properties props = FabricBlockSettings
                .of(mat)
                .sounds(SoundType.GRASS)
                .noCollision()
                .offsetType(BlockBehaviour.OffsetType.XZ);
        if (light > 0) props.lightLevel(s -> light);
        return props;
    }

    private static final VoxelShape SHAPE = box(4, 0, 4, 12, 14, 12);

    public BasePlantBlock() {
        this(basePlantSettings());
    }

    public BasePlantBlock(int light) {
        this(basePlantSettings(light));
    }

    @Deprecated(forRemoval = true)
    public BasePlantBlock(int light, SettingsExtender propMod) {
        this(false, light, propMod);
    }

    public BasePlantBlock(boolean replaceable) {
        this(basePlantSettings(replaceable));
    }

    @Deprecated(forRemoval = true)
    public BasePlantBlock(boolean replaceable, SettingsExtender propMod) {
        this(replaceable, 0, propMod);
    }


    public BasePlantBlock(boolean replaceable, int light) {
        this(basePlantSettings(replaceable, light));
    }

    @Deprecated(forRemoval = true)
    public BasePlantBlock(boolean replaceable, int light, SettingsExtender propMod) {
        this(
                propMod.amend(FabricBlockSettings
                        .of(replaceable ? Material.REPLACEABLE_PLANT : Material.PLANT)
                        .luminance(light)
                        .sound(SoundType.GRASS)
                        .noCollission()
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                )
        );
    }

    protected BasePlantBlock(Properties settings) {
        super(settings);
    }

    protected abstract boolean isTerrain(BlockState state);


    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        Vec3 vec3d = state.getOffset(view, pos);
        return SHAPE.move(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState down = level.getBlockState(pos.below());
        return isTerrain(down);
    }

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
            return Blocks.AIR.defaultBlockState();
        } else {
            return state;
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
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
    public boolean isValidBonemealTarget(BlockGetter world, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, Random random, BlockPos pos, BlockState state) {
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
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return ModelsHelper.createBlockItem(resourceLocation);
    }

    @Override
    @Nullable
    @Environment(EnvType.CLIENT)
    public BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_CROSS, resourceLocation);
        return ModelsHelper.fromPattern(pattern);
    }
}
