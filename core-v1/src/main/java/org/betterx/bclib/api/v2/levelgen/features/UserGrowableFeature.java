package org.betterx.bclib.api.v2.levelgen.features;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * @param <FC>
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.UserGrowableFeature} instead
 */
@Deprecated(forRemoval = true)
public interface UserGrowableFeature<FC extends FeatureConfiguration> extends org.betterx.bclib.api.v3.levelgen.features.UserGrowableFeature<FC> {
}
