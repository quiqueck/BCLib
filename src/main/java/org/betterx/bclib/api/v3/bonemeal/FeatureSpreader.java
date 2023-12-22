package org.betterx.bclib.api.v3.bonemeal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import org.jetbrains.annotations.Nullable;

public class FeatureSpreader implements BonemealNyliumLike {
    public final BonemealAPI.FeatureProvider spreadableFeature;
    public final Block hostBlock;

    public FeatureSpreader(Block hostBlock, BonemealAPI.FeatureProvider spreadableFeature) {
        this.spreadableFeature = spreadableFeature;
        this.hostBlock = hostBlock;
    }

    @Override
    public boolean isValidBonemealTarget(
            LevelReader blockGetter,
            BlockPos blockPos,
            BlockState blockState
    ) {
        return spreadableFeature != null
                && BonemealNyliumLike.super.isValidBonemealTarget(blockGetter, blockPos, blockState);
    }

    @Override
    public Block getHostBlock() {
        return hostBlock;
    }

    @Override
    public @Nullable Holder<? extends ConfiguredFeature<?, ?>> getCoverFeature() {
        return spreadableFeature.getFeature();
    }
}
