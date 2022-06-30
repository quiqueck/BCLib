package org.betterx.bclib.api.v3.levelgen.features;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Random;

public interface UserGrowableFeature<FC extends FeatureConfiguration> {
    boolean grow(
            ServerLevelAccessor level,
            BlockPos pos,
            Random random,
            FC configuration
    );
}
