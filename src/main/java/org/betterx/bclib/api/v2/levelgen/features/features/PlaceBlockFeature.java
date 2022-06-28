package org.betterx.bclib.api.v2.levelgen.features.features;

import org.betterx.bclib.api.v3.levelgen.features.config.PlaceBlockFeatureConfig;

import com.mojang.serialization.Codec;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.features.PlaceBlockFeature} instead.
 */
@Deprecated(forRemoval = true)
public class PlaceBlockFeature<FC extends PlaceBlockFeatureConfig> extends org.betterx.bclib.api.v3.levelgen.features.features.PlaceBlockFeature<FC> {

    public PlaceBlockFeature(Codec<FC> codec) {
        super(codec);
    }
}
