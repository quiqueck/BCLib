package org.betterx.bclib.api.v3.levelgen.features.features;

import org.betterx.bclib.api.v2.levelgen.structures.StructureNBT;
import org.betterx.bclib.api.v2.levelgen.structures.StructureWorldNBT;
import org.betterx.bclib.api.v3.levelgen.features.config.TemplateFeatureConfig;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class TemplateFeature<FC extends TemplateFeatureConfig> extends Feature<FC> {
    public TemplateFeature(Codec<FC> codec) {
        super(codec);
    }

    protected StructureWorldNBT randomStructure(TemplateFeatureConfig cfg, RandomSource random) {
        if (cfg.structures.size() > 1) {
            final float chanceSum = cfg.structures.parallelStream().map(c -> c.chance).reduce(0.0f, (p, c) -> p + c);
            float rnd = random.nextFloat() * chanceSum;

            for (StructureWorldNBT c : cfg.structures) {
                rnd -= c.chance;
                if (rnd <= 0) return c;
            }
        } else {
            return cfg.structures.get(0);
        }

        return null;
    }

    @Override
    public boolean place(FeaturePlaceContext<FC> ctx) {
        StructureWorldNBT structure = randomStructure(ctx.config(), ctx.random());
        return structure.generateIfPlaceable(
                ctx.level(),
                ctx.origin(),
                StructureNBT.getRandomRotation(ctx.random()),
                StructureNBT.getRandomMirror(ctx.random())
        );
    }
}
