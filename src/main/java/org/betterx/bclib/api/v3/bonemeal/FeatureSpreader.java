package org.betterx.bclib.api.v3.bonemeal;

import org.betterx.bclib.api.v3.levelgen.features.BCLConfigureFeature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

public class FeatureSpreader implements BonemealNyliumLike {
    public final BCLConfigureFeature<? extends Feature<?>, ?> spreadableFeature;
    public final Block hostBlock;

    public FeatureSpreader(Block hostBlock, BCLConfigureFeature<? extends Feature<?>, ?> spreadableFeature) {
        this.spreadableFeature = spreadableFeature;
        this.hostBlock = hostBlock;
    }

    @Override
    public boolean isValidBonemealTarget(
            LevelReader blockGetter,
            BlockPos blockPos,
            BlockState blockState,
            boolean bl
    ) {
        return spreadableFeature != null
                && BonemealNyliumLike.super.isValidBonemealTarget(blockGetter, blockPos, blockState, bl);
    }

    @Override
    public Block getHostBlock() {
        return hostBlock;
    }

    @Override
    public BCLConfigureFeature<? extends Feature<?>, ?> getCoverFeature() {
        return spreadableFeature;
    }
}
