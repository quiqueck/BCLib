package org.betterx.bclib.api.v3.levelgen.features.features;

import org.betterx.bclib.api.v3.levelgen.features.config.PillarFeatureConfig;
import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class PillarFeature extends Feature<PillarFeatureConfig> {
    public PillarFeature() {
        super(PillarFeatureConfig.CODEC);
    }


    @Override
    public boolean place(FeaturePlaceContext<PillarFeatureConfig> featurePlaceContext) {
        int height;
        final WorldGenLevel level = featurePlaceContext.level();
        final PillarFeatureConfig config = featurePlaceContext.config();
        final RandomSource rnd = featurePlaceContext.random();
        int maxHeight = config.maxHeight.sample(rnd);
        int minHeight = config.minHeight.sample(rnd);
        BlockPos.MutableBlockPos posnow = featurePlaceContext.origin().mutable();
        posnow.move(config.direction);

        for (height = 1; height < maxHeight; ++height) {
            if (!config.allowedPlacement.test(level, posnow)) {
                maxHeight = height;
                break;
            }
            posnow.move(config.direction);
        }
        if (maxHeight < minHeight) return false;

        if (!config.transformer.canPlace.at(
                minHeight,
                maxHeight,
                featurePlaceContext.origin(),
                posnow,
                level,
                config.allowedPlacement,
                rnd
        )) {
            return false;
        }
        posnow = featurePlaceContext.origin().mutable();
        for (height = 0; height < maxHeight; ++height) {
            BlockState state = config.transform(height, maxHeight - 1, posnow, rnd);
            BlocksHelper.setWithoutUpdate(level, posnow, state);
            posnow.move(config.direction);
        }

        return true;
    }
}
