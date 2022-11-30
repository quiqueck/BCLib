package org.betterx.bclib.api.v3.levelgen.features.placement;

import org.betterx.bclib.noise.Noises;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseFilter extends PlacementFilter {
    public static final Codec<NoiseFilter> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    ResourceKey.codec(Registries.NOISE).fieldOf("noise").forGetter(o -> o.noise),
                    Codec.DOUBLE.fieldOf("min_noise_level").forGetter(o -> o.minNoiseLevel),
                    Codec.DOUBLE.fieldOf("max_noise_level").orElse(Double.MAX_VALUE).forGetter(o -> o.maxNoiseLevel),
                    Codec.FLOAT.fieldOf("scale_xz").orElse(1f).forGetter(o -> o.scaleXZ),
                    Codec.FLOAT.fieldOf("scale_y").orElse(1f).forGetter(o -> o.scaleY)
            )
            .apply(instance, NoiseFilter::new));

    private final ResourceKey<NormalNoise.NoiseParameters> noise;

    private final double minNoiseLevel;
    private final double maxNoiseLevel;
    private final float scaleXZ;
    private final float scaleY;


    public NoiseFilter(
            ResourceKey<NormalNoise.NoiseParameters> noise,
            double minNoiseLevel,
            double maxNoiseLevel,
            float scaleXZ,
            float scaleY
    ) {
        this.noise = noise;
        this.minNoiseLevel = minNoiseLevel;
        this.maxNoiseLevel = maxNoiseLevel;
        this.scaleXZ = scaleXZ;
        this.scaleY = scaleY;
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        final NormalNoise normalNoise = Noises.getOrCreateNoise(ctx.getLevel().registryAccess(), random, this.noise);
        final double v = normalNoise.getValue(pos.getX() * scaleXZ, pos.getY() * scaleY, pos.getZ() * scaleXZ);
        return v > minNoiseLevel && v < maxNoiseLevel;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.NOISE_FILTER;
    }
}
