package org.betterx.bclib.api.v2.levelgen.features.features;

import org.betterx.bclib.api.v3.levelgen.features.config.TemplateFeatureConfig;

import com.mojang.serialization.Codec;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.features.TemplateFeature} instead.
 */
@Deprecated(forRemoval = true)
public class TemplateFeature<FC extends TemplateFeatureConfig> extends org.betterx.bclib.api.v3.levelgen.features.features.TemplateFeature<FC> {

    public TemplateFeature(Codec<FC> codec) {
        super(codec);
    }
}
