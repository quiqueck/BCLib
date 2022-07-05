package org.betterx.bclib.api.v2.levelgen.surface.rules;

import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;
import org.betterx.bclib.noise.OpenSimplexNoise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;

import com.google.common.collect.Maps;

import java.util.Map;

public class ThresholdCondition extends SurfaceNoiseCondition {
    private static final Map<Long, Context> NOISES = Maps.newHashMap();
    public static final Codec<ThresholdCondition> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.LONG.fieldOf("seed").forGetter(p -> p.noiseContext.seed),
                    Codec.DOUBLE.fieldOf("threshold").orElse(0.0).forGetter(p -> p.threshold),
                    FloatProvider.CODEC.fieldOf("threshold_offset").orElse(ConstantFloat.of(0)).forGetter(p -> p.range),
                    Codec.DOUBLE.fieldOf("scale_x").orElse(0.1).forGetter(p -> p.scaleX),
                    Codec.DOUBLE.fieldOf("scale_z").orElse(0.1).forGetter(p -> p.scaleZ)
            )
            .apply(instance, ThresholdCondition::new));
    public static final KeyDispatchDataCodec<ThresholdCondition> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);
    private final Context noiseContext;
    private final double threshold;
    private final FloatProvider range;
    private final double scaleX;
    private final double scaleZ;

    public ThresholdCondition(long noiseSeed, double threshold, FloatProvider range, double scaleX, double scaleZ) {
        this.threshold = threshold;
        this.range = range;
        this.scaleX = scaleX;
        this.scaleZ = scaleZ;

        noiseContext = NOISES.computeIfAbsent(noiseSeed, seed -> new Context(seed));
    }

    @Override
    public boolean test(SurfaceRulesContextAccessor context) {
        final double x = context.getBlockX() * scaleX;
        final double z = context.getBlockZ() * scaleZ;
        if (noiseContext.lastX == x && noiseContext.lastZ == z)
            return noiseContext.lastValue + range.sample(noiseContext.random) > threshold;
        double value = noiseContext.noise.eval(x, z);

        noiseContext.lastX = x;
        noiseContext.lastZ = z;
        noiseContext.lastValue = value;
        return value + range.sample(noiseContext.random) > threshold;
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return KEY_CODEC;
    }

    static class Context {
        public final OpenSimplexNoise noise;
        public final RandomSource random;
        public final long seed;

        public double lastX = Integer.MIN_VALUE;
        public double lastZ = Integer.MIN_VALUE;
        public double lastValue = 0;

        Context(long seed) {
            this.seed = seed;
            this.noise = new OpenSimplexNoise(seed);
            this.random = new ThreadSafeLegacyRandomSource(seed * 2);
        }
    }
}
