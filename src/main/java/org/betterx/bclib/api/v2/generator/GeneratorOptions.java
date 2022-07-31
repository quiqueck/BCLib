package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.config.Configs;

public class GeneratorOptions {
    //private static BiFunction<Point, Integer, Boolean> endLandFunction;
    private static boolean fixEndBiomeSource = true;
    private static boolean fixNetherBiomeSource = true;

    public static void init() {
        fixEndBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options.biomeSource", "fixEndBiomeSource", true);
        fixNetherBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options.biomeSource", "fixNetherBiomeSource", true);
    }

    public static boolean fixEndBiomeSource() {
        return fixEndBiomeSource;
    }

    public static boolean fixNetherBiomeSource() {
        return fixNetherBiomeSource;
    }
}
