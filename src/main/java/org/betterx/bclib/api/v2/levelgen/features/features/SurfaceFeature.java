package org.betterx.bclib.api.v2.levelgen.features.features;

import org.betterx.bclib.util.BlocksHelper;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Optional;

public abstract class SurfaceFeature<T extends FeatureConfiguration> extends Feature<T> {
    public static abstract class DefaultConfiguration extends SurfaceFeature<NoneFeatureConfiguration> {
        protected DefaultConfiguration() {
            super(NoneFeatureConfiguration.CODEC);
        }
    }

    protected SurfaceFeature(Codec<T> codec) {
        super(codec);
    }

    protected abstract boolean isValidSurface(BlockState state);

    protected int minHeight(FeaturePlaceContext<T> ctx) {
        return ctx.chunkGenerator().getSeaLevel();
    }

    @Override
    public boolean place(FeaturePlaceContext<T> ctx) {
        Optional<BlockPos> pos = BlocksHelper.findSurfaceBelow(
                ctx.level(),
                ctx.origin(),
                minHeight(ctx),
                this::isValidSurface
        );
        if (pos.isPresent()) {
            generate(pos.get(), ctx);
            return true;
        }


        return false;
    }

    protected abstract void generate(BlockPos centerPos, FeaturePlaceContext<T> ctx);
}
