package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.config.Configs;

import net.minecraft.util.Mth;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GeneratorOptions {
    private static int biomeSizeNether;
    private static int biomeVSizeNether;
    private static int biomeSizeEndLand;
    private static int biomeSizeEndVoid;
    private static BiFunction<Point, Integer, Boolean> endLandFunction;
    private static boolean customNetherBiomeSource = true;
    private static boolean customEndBiomeSource = true;
    private static boolean verticalBiomes = true;
    private static long farEndBiomesSqr = 1000000;
    private static boolean fixEndBiomeSource = true;
    private static boolean fixNetherBiomeSource = true;

    public static void init() {
        biomeSizeNether = Configs.GENERATOR_CONFIG.getInt("nether.biomeMap", "biomeSize", 256);
        biomeVSizeNether = Configs.GENERATOR_CONFIG.getInt(
                "nether.biomeMap",
                "biomeVerticalSize(onlyInTallNether)",
                86
        );
        biomeSizeEndLand = Configs.GENERATOR_CONFIG.getInt("end.biomeMap", "biomeSizeLand", 256);
        biomeSizeEndVoid = Configs.GENERATOR_CONFIG.getInt("end.biomeMap", "biomeSizeVoid", 256);
        customNetherBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options", "customNetherBiomeSource", true);
        customEndBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options", "customEndBiomeSource", true);
        verticalBiomes = Configs.GENERATOR_CONFIG.getBoolean("options", "verticalBiomesInTallNether", true);
        fixEndBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options.biomeSource", "fixEndBiomeSource", true);
        fixNetherBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options.biomeSource", "fixNetherBiomeSource", true);
    }

    public static int getBiomeSizeNether() {
        return Mth.clamp(biomeSizeNether, 1, 8192);
    }

    public static int getVerticalBiomeSizeNether() {
        return Mth.clamp(biomeVSizeNether, 1, 8192);
    }

    public static int getBiomeSizeEndLand() {
        return Mth.clamp(biomeSizeEndLand, 1, 8192);
    }

    public static int getBiomeSizeEndVoid() {
        return Mth.clamp(biomeSizeEndVoid, 1, 8192);
    }

    /**
     * @param endLandFunction
     * @deprecated use {@link #setEndLandFunction(BiFunction)} instead
     */
    @Deprecated(forRemoval = true)
    public static void setEndLandFunction(Function<Point, Boolean> endLandFunction) {
        GeneratorOptions.endLandFunction = (p, h) -> endLandFunction.apply(p);
    }

    public static void setEndLandFunction(BiFunction<Point, Integer, Boolean> endLandFunction) {
        GeneratorOptions.endLandFunction = endLandFunction;
    }

    public static BiFunction<Point, Integer, Boolean> getEndLandFunction() {
        return endLandFunction;
    }

    public static long getFarEndBiomes() {
        return farEndBiomesSqr;
    }

    /**
     * Set distance of far End biomes generation, in blocks
     *
     * @param distance
     */
    public static void setFarEndBiomes(int distance) {
        GeneratorOptions.farEndBiomesSqr = (long) distance * (long) distance;
    }

    /**
     * Set distance of far End biomes generation, in blocks^2
     *
     * @param distanceSqr the distance squared
     */
    public static void setFarEndBiomesSqr(long distanceSqr) {
        GeneratorOptions.farEndBiomesSqr = distanceSqr;
    }

    public static boolean customNetherBiomeSource() {
        return customNetherBiomeSource;
    }

    public static boolean customEndBiomeSource() {
        return customEndBiomeSource;
    }


    public static boolean useVerticalBiomes() {
        return verticalBiomes;
    }

    public static boolean fixEndBiomeSource() {
        return fixEndBiomeSource;
    }

    public static boolean fixNetherBiomeSource() {
        return fixNetherBiomeSource;
    }
}
