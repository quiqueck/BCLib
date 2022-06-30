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

public class VolumeThresholdCondition extends VolumeNoiseCondition {
    private static final Map<Long, VolumeThresholdCondition.Context> NOISES = Maps.newHashMap();
    public static final Codec<VolumeThresholdCondition> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.LONG.fieldOf("seed").forGetter(p -> p.noiseContext.seed),
                    Codec.DOUBLE.fieldOf("threshold").orElse(0.0).forGetter(p -> p.threshold),
                    FloatProvider.CODEC.fieldOf("threshold_offset").orElse(ConstantFloat.of(0)).forGetter(p -> p.range),
                    Codec.DOUBLE.fieldOf("scale_x").orElse(0.1).forGetter(p -> p.scaleX),
                    Codec.DOUBLE.fieldOf("scale_y").orElse(0.1).forGetter(p -> p.scaleY),
                    Codec.DOUBLE.fieldOf("scale_z").orElse(0.1).forGetter(p -> p.scaleZ)
            )
            .apply(instance, VolumeThresholdCondition::new));
    public static final KeyDispatchDataCodec<VolumeThresholdCondition> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);
    public final VolumeThresholdCondition.Context noiseContext;
    public final double threshold;
    public final FloatProvider range;
    public final double scaleX;
    public final double scaleY;
    public final double scaleZ;

    public VolumeThresholdCondition(
            long noiseSeed,
            double threshold,
            FloatProvider range,
            double scaleX,
            double scaleY,
            double scaleZ
    ) {
        this.threshold = threshold;
        this.range = range;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;

        noiseContext = NOISES.computeIfAbsent(noiseSeed, seed -> new Context(seed));
    }

    public double getValue(SurfaceRulesContextAccessor context) {
        return getValue(context.getBlockX(), context.getBlockY(), context.getBlockZ());
    }

    public double getValue(int xx, int yy, int zz) {
        final double x = xx * scaleX;
        final double y = yy * scaleY;
        final double z = zz * scaleZ;

        if (noiseContext.lastX == x
                && noiseContext.lastY == y
                && noiseContext.lastZ == z)
            return noiseContext.lastValue + range.sample(noiseContext.random);

        double value = noiseContext.noise.eval(x, y, z);

        noiseContext.lastX = x;
        noiseContext.lastZ = z;
        noiseContext.lastY = y;
        noiseContext.lastValue = value;

        return value + range.sample(noiseContext.random);
    }

    @Override
    public boolean test(SurfaceRulesContextAccessor context) {
        return getValue(context) > threshold;
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return KEY_CODEC;
    }

    public static class Context {
        public final OpenSimplexNoise noise;
        public final RandomSource random;
        public final long seed;

        double lastX = Integer.MIN_VALUE;
        double lastY = Integer.MIN_VALUE;
        double lastZ = Integer.MIN_VALUE;
        double lastValue = 0;

        Context(long seed) {
            this.seed = seed;
            this.noise = new OpenSimplexNoise(seed);
            this.random = new ThreadSafeLegacyRandomSource(seed * 3 + 1);
        }
    }
}
