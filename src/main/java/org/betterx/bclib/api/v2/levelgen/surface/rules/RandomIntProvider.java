package org.betterx.bclib.api.v2.levelgen.surface.rules;

import org.betterx.bclib.interfaces.NumericProvider;
import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;
import org.betterx.bclib.util.MHelper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record RandomIntProvider(int range) implements NumericProvider {
    public static final MapCodec<RandomIntProvider> CODEC = Codec.INT.fieldOf("range")
                                                                     .xmap(RandomIntProvider::new, obj -> obj.range);

    @Override
    public int getNumber(SurfaceRulesContextAccessor context) {
        return MHelper.RANDOM.nextInt(range);
    }

    @Override
    public MapCodec<? extends NumericProvider> pcodec() {
        return CODEC;
    }

    static {

    }
}
