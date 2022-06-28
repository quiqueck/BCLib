package org.betterx.bclib.api.v3.levelgen.features.features;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MarkPostProcessingFeature extends Feature<NoneFeatureConfiguration> {
    public MarkPostProcessingFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        BlockPos pos = ctx.origin();
        ctx.level().getChunk(pos.getX() >> 4, pos.getZ() >> 4)
           .markPosForPostprocessing(new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15));
        return true;
    }
}
