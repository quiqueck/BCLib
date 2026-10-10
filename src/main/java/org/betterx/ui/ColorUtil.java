package org.betterx.ui;

import de.ambertation.wunderlib.utils.ColorUtilARGB32;
import org.betterx.bclib.client.ClientColorUtil;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * @deprecated Use {@link ColorUtilARGB32}, or {@link ClientColorUtil} for the texture based helpers.
 */
@Deprecated(forRemoval = true)
public class ColorUtil {
    public static final int BLACK = ColorUtilARGB32.BLACK;
    public static final int DARK_BLUE = ColorUtilARGB32.DARK_BLUE;
    public static final int DARK_GREEN = ColorUtilARGB32.DARK_GREEN;
    public static final int DARK_AQUA = ColorUtilARGB32.DARK_AQUA;
    public static final int DARK_RED = ColorUtilARGB32.DARK_RED;
    public static final int DARK_PURPLE = ColorUtilARGB32.DARK_PURPLE;
    public static final int GOLD = ColorUtilARGB32.GOLD;
    public static final int GRAY = ColorUtilARGB32.GRAY;
    public static final int DARK_GRAY = ColorUtilARGB32.DARK_GRAY;
    public static final int BLUE = ColorUtilARGB32.BLUE;
    public static final int GREEN = ColorUtilARGB32.GREEN;
    public static final int AQUA = ColorUtilARGB32.AQUA;
    public static final int RED = ColorUtilARGB32.RED;
    public static final int LIGHT_PURPLE = ColorUtilARGB32.LIGHT_PURPLE;
    public static final int YELLOW = ColorUtilARGB32.YELLOW;
    public static final int WHITE = ColorUtilARGB32.WHITE;
    public static final int DEFAULT_TEXT = ColorUtilARGB32.DEFAULT_TEXT;

    public static int color(int r, int g, int b) {
        return ColorUtilARGB32.color(r, g, b);
    }

    public static int color(String hex) {
        return ColorUtilARGB32.color(hex);
    }

    public static int[] toIntArray(int color) {
        return ColorUtilARGB32.toIntArrayARGB(color);
    }

    public static float[] toFloatArray(int color) {
        return ColorUtilARGB32.toFloatArrayRGBA(color);
    }

    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        return ColorUtilARGB32.RGBtoHSB(r, g, b, hsbvals);
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        return ColorUtilARGB32.HSBtoRGB(hue, saturation, brightness);
    }

    public static String toRGBHex(int color) {
        return ColorUtilARGB32.toRGBHex(color);
    }

    public static boolean validHexColor(String hexColor) {
        return ColorUtilARGB32.validHexColor(hexColor);
    }

    public static int parseHex(String hexColor) {
        return ColorUtilARGB32.parseHex(hexColor);
    }

    public static int toABGR(int color) {
        return ColorUtilARGB32.toABGR(color);
    }

    public static int ABGRtoARGB(int color) {
        return ColorUtilARGB32.ABGRtoARGB(color);
    }

    public static int colorBrigtness(int color, float val) {
        return ColorUtilARGB32.colorBrightness(color, val);
    }

    public static int multiplyColor(int color1, int color2) {
        return ColorUtilARGB32.multiplyColor(color1, color2);
    }

    public static int applyTint(int color, int tint) {
        return ColorUtilARGB32.applyTint(color, tint);
    }

    public static int colorDistance(int color1, int color2) {
        return ColorUtilARGB32.colorDistance(color1, color2);
    }

    @Environment(EnvType.CLIENT)
    public static int extractColor(Item item) {
        return ClientColorUtil.extractColor(item);
    }

    @Environment(EnvType.CLIENT)
    public static NativeImage loadImage(Identifier image, int w, int h) {
        return ClientColorUtil.loadImage(image, w, h);
    }
}
