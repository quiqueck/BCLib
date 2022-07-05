package org.betterx.bclib.api.v2.levelgen.features.config;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.config.SequenceFeatureConfig instead}
 */
@Deprecated(forRemoval = true)
public class SequenceFeatureConfig extends org.betterx.bclib.api.v3.levelgen.features.config.SequenceFeatureConfig {

    public SequenceFeatureConfig(List<Holder<PlacedFeature>> features) {
        super(features);
    }
}
