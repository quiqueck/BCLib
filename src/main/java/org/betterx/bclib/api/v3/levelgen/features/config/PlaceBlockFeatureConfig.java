package org.betterx.bclib.api.v3.levelgen.features.config;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

import java.util.List;

public abstract class PlaceBlockFeatureConfig implements FeatureConfiguration {

    protected static <T extends PlaceBlockFeatureConfig> RecordCodecBuilder<T, BlockStateProvider> blockStateCodec() {
        return BlockStateProvider.CODEC
                .fieldOf("entries")
                .forGetter((T o) -> o.stateProvider);
    }

    protected final BlockStateProvider stateProvider;


    protected static SimpleWeightedRandomList<BlockState> buildWeightedList(List<BlockState> states) {
        var builder = SimpleWeightedRandomList.<BlockState>builder();
        for (BlockState s : states) builder.add(s, 1);
        return builder.build();
    }

    protected static SimpleWeightedRandomList<BlockState> buildWeightedList(BlockState state) {
        return SimpleWeightedRandomList
                .<BlockState>builder()
                .add(state, 1)
                .build();
    }

    public PlaceBlockFeatureConfig(Block block) {
        this(block.defaultBlockState());
    }

    public PlaceBlockFeatureConfig(BlockState state) {
        this(BlockStateProvider.simple(state));
    }


    public PlaceBlockFeatureConfig(List<BlockState> states) {
        this(buildWeightedList(states));
    }

    public PlaceBlockFeatureConfig(SimpleWeightedRandomList<BlockState> blocks) {
        this.stateProvider = new WeightedStateProvider(blocks);
    }

    public PlaceBlockFeatureConfig(BlockStateProvider blocks) {
        this.stateProvider = blocks;
    }

    public BlockState getRandomBlock(RandomSource random, BlockPos pos) {
        return this.stateProvider.getState(random, pos);
    }

    public boolean place(FeaturePlaceContext<? extends PlaceBlockFeatureConfig> ctx) {
        BlockState state = getRandomBlock(ctx.random(), ctx.origin());
        return placeBlock(ctx, ctx.level(), ctx.origin(), state);
    }


    protected abstract boolean placeBlock(
            FeaturePlaceContext<? extends PlaceBlockFeatureConfig> ctx,
            WorldGenLevel level,
            BlockPos pos,
            BlockState targetState
    );
}
