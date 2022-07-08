package org.betterx.bclib.api.v2.generator.config;

import org.betterx.bclib.api.v2.generator.BiomePicker;
import org.betterx.bclib.interfaces.BiomeMap;

@FunctionalInterface
public interface MapBuilderFunction {
    BiomeMap create(long seed, int biomeSize, BiomePicker picker);
}
