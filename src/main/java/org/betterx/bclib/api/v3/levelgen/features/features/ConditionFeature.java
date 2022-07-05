package org.betterx.bclib.api.v3.levelgen.features.features;

import org.betterx.bclib.api.v3.levelgen.features.config.ConditionFeatureConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

import java.util.Optional;
import java.util.stream.Stream;

public class ConditionFeature extends Feature<ConditionFeatureConfig> {
    public ConditionFeature() {
        super(ConditionFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<ConditionFeatureConfig> ctx) {
        final ConditionFeatureConfig cfg = ctx.config();
        final WorldGenLevel level = ctx.level();
        final RandomSource random = ctx.random();
        final BlockPos pos = ctx.origin();

        final PlacementContext c = new PlacementContext(level, ctx.chunkGenerator(), Optional.empty());

        Stream<BlockPos> stream = cfg.filter.getPositions(c, ctx.random(), pos);
        Holder<PlacedFeature> state = (stream.findFirst().isPresent() ? cfg.okFeature : cfg.failFeature.orElse(null));
        if (state != null) {
            return state.value().place(level, ctx.chunkGenerator(), random, pos);
        }
        return false;
    }
}
