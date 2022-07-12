package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.levelgen.features.BCLConfigureFeature;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.RenderLayerProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class FeatureSaplingBlock<F extends Feature<FC>, FC extends FeatureConfiguration> extends SaplingBlock implements RenderLayerProvider, BlockModelProvider {

    @FunctionalInterface
    public interface FeatureSupplier<F extends Feature<FC>, FC extends FeatureConfiguration> {
        BCLConfigureFeature<F, FC> get(BlockState state);
    }

    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 14, 12);
    private final FeatureSupplier<F, FC> feature;

    public FeatureSaplingBlock(FeatureSupplier<F, FC> featureSupplier) {
        this(
                BlockBehaviour.Properties.of(Material.PLANT)
                                         .noCollission()
                                         .instabreak()
                                         .sound(SoundType.GRASS)
                                         .randomTicks(),
                featureSupplier
        );
    }

    public FeatureSaplingBlock(int light, FeatureSupplier<F, FC> featureSupplier) {
        this(
                BlockBehaviour.Properties.of(Material.PLANT)
                                         .noCollission()
                                         .lightLevel(state -> light)
                                         .instabreak()
                                         .sound(SoundType.GRASS)
                                         .randomTicks(),
                featureSupplier
        );
    }

    public FeatureSaplingBlock(
            BlockBehaviour.Properties properties,
            FeatureSupplier<F, FC> featureSupplier
    ) {
        super(null, properties);
        this.feature = featureSupplier;
    }

    protected BCLConfigureFeature<F, FC> getConfiguredFeature(BlockState state) {
        return feature != null ? feature.get(state) : null;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
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
        if (!canSurvive(state, world, pos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextInt(16) == 0;
    }

    @Override
    public void advanceTree(ServerLevel world, BlockPos pos, BlockState blockState, RandomSource random) {
        BCLConfigureFeature<F, FC> conf = getConfiguredFeature(blockState);
        conf.placeInWorld(world, pos, random);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        this.tick(state, world, pos, random);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        if (isBonemealSuccess(world, random, pos, state)) {
            performBonemeal(world, random, pos, state);
        }
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return ModelsHelper.createBlockItem(resourceLocation);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_CROSS, resourceLocation);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }
}
