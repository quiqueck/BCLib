package org.betterx.bclib.api.v3.bonemeal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

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
            BlockState blockState,
            BonemealSource bonemealSource
    ) {
        return spreadableFeature != null
                && BonemealNyliumLike.super.isValidBonemealTarget(
                blockGetter,
                blockPos,
                blockState,
                bonemealSource
        );
    }

    @Override
    public Block getHostBlock() {
        return hostBlock;
    }

    @Override
    public @Nullable Holder<Feature> getCoverFeature() {
        return spreadableFeature.getFeature();
    }
}
