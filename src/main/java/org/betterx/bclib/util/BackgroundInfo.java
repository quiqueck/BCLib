package org.betterx.bclib.util;

public class BackgroundInfo {
    public static float fogColorRed;
    public static float fogColorGreen;
    public static float fogColorBlue;
    public static float fogDensity = 1;
    public static float blindness;

    /**
     * Colour of the custom End sky at the horizon. A sky renderer sets it while it draws, so the End's fog can fade
     * into the sky where biome fog is thin. Ignored while {@link #hasSkyHorizonColor} is false.
     */
    public static float skyHorizonRed;
    public static float skyHorizonGreen;
    public static float skyHorizonBlue;
    public static boolean hasSkyHorizonColor;
}
