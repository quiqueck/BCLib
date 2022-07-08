package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.config.Configs;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GeneratorOptions {
    //private static BiFunction<Point, Integer, Boolean> endLandFunction;
    private static boolean fixEndBiomeSource = true;
    private static boolean fixNetherBiomeSource = true;

    public static void init() {
        fixEndBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options.biomeSource", "fixEndBiomeSource", true);
        fixNetherBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options.biomeSource", "fixNetherBiomeSource", true);
    }

    @Deprecated(forRemoval = true)
    public static int getBiomeSizeNether() {
        return 256;
    }

    @Deprecated(forRemoval = true)
    public static int getVerticalBiomeSizeNether() {
        return 86;
    }

    @Deprecated(forRemoval = true)
    public static int getBiomeSizeEndLand() {
        return 256;
    }

    @Deprecated(forRemoval = true)
    public static int getBiomeSizeEndVoid() {
        return 256;
    }

    /**
     * @param endLandFunction
     * @deprecated use {@link #setEndLandFunction(BiFunction)} instead
     */
    @Deprecated(forRemoval = true)
    public static void setEndLandFunction(Function<Point, Boolean> endLandFunction) {
        //GeneratorOptions.endLandFunction = (p, h) -> endLandFunction.apply(p);
    }

    @Deprecated(forRemoval = true)
    public static void setEndLandFunction(BiFunction<Point, Integer, Boolean> endLandFunction) {
        ///GeneratorOptions.endLandFunction = endLandFunction;
    }

    @Deprecated(forRemoval = true)
    public static BiFunction<Point, Integer, Boolean> getEndLandFunction() {
        return (a, b) -> true;//endLandFunction;
    }

    @Deprecated(forRemoval = true)
    public static long getFarEndBiomes() {
        return 1000000;
    }

    /**
     * Set distance of far End biomes generation, in blocks
     *
     * @param distance
     */
    @Deprecated(forRemoval = true)
    public static void setFarEndBiomes(int distance) {
    }

    /**
     * Set distance of far End biomes generation, in blocks^2
     *
     * @param distanceSqr the distance squared
     */
    @Deprecated(forRemoval = true)
    public static void setFarEndBiomesSqr(long distanceSqr) {

    }

    @Deprecated(forRemoval = true)
    public static boolean customNetherBiomeSource() {
        return true;
    }

    @Deprecated(forRemoval = true)
    public static boolean customEndBiomeSource() {
        return true;
    }


    @Deprecated(forRemoval = true)
    public static boolean useVerticalBiomes() {
        return true;
    }

    public static boolean fixEndBiomeSource() {
        return fixEndBiomeSource;
    }

    public static boolean fixNetherBiomeSource() {
        return fixNetherBiomeSource;
    }
}
