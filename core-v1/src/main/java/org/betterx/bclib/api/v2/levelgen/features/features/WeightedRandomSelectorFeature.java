package org.betterx.bclib.api.v2.levelgen.features.features;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;


@Deprecated(forRemoval = true)
public class WeightedRandomSelectorFeature extends Feature<RandomFeatureConfiguration> {
    public WeightedRandomSelectorFeature() {
        super(RandomFeatureConfiguration.CODEC);
    }

    public boolean place(FeaturePlaceContext<RandomFeatureConfiguration> ctx) {
        final WorldGenLevel level = ctx.level();
        final ChunkGenerator generator = ctx.chunkGenerator();
        final RandomFeatureConfiguration cfg = ctx.config();
        final RandomSource random = ctx.random();
        final BlockPos pos = ctx.origin();

        PlacedFeature selected = cfg.defaultFeature.value();
        if (!cfg.features.isEmpty()) {
            final float totalWeight = cfg.features.stream().map(w -> w.chance).reduce(0.0f, (p, c) -> p + c);
            float bar = random.nextFloat() * totalWeight;

            for (WeightedPlacedFeature f : cfg.features) {
                selected = f.feature.value();
                bar -= f.chance;
                if (bar < 0) break;
            }
        }
        return selected.place(level, generator, random, pos);
    }
}
