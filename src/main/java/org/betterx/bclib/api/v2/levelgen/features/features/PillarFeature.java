package org.betterx.bclib.api.v2.levelgen.features.features;

import org.betterx.bclib.api.v2.levelgen.features.config.PillarFeatureConfig;
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
        int maxHeight = config.height.sample(rnd);

        BlockPos.MutableBlockPos posnow = featurePlaceContext.origin().mutable();

        for (height = 0; height < maxHeight; ++height) {
            if (!config.allowedPlacement.test(level, posnow)) {
                maxHeight = height - 1;
                break;
            }
            posnow.move(config.direction);
        }
        if (maxHeight < 0) return false;

        posnow = featurePlaceContext.origin().mutable();
        for (height = 0; height < maxHeight; ++height) {
            BlockState state = config.transform(height, maxHeight, posnow, rnd);
            BlocksHelper.setWithoutUpdate(level, posnow, state);
            posnow.move(config.direction);
        }

        return true;
    }
}
