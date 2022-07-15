package org.betterx.bclib.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @deprecated Please use {@link org.betterx.ui.ColorUtil} instead
 */
@Deprecated(forRemoval = true)
public class ColorUtil {
    private static final float[] FLOAT_BUFFER = new float[4];
    private static final int ALPHA = 255 << 24;

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#color(int, int, int)} instead
     */
    @Deprecated(forRemoval = true)
    public static int color(int r, int g, int b) {
        return ALPHA | (r << 16) | (g << 8) | b;
    }


    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#color(String)} instead
     */
    @Deprecated(forRemoval = true)
    public static int color(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return color(r, g, b);
    }


    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#toIntArray(int)} instead
     */
    @Deprecated(forRemoval = true)
    public static int[] toIntArray(int color) {
        return new int[]{(color >> 24) & 255, (color >> 16) & 255, (color >> 8) & 255, color & 255};
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#toFloatArray(int)} instead
     */
    @Deprecated(forRemoval = true)
    public static float[] toFloatArray(int color) {
        FLOAT_BUFFER[0] = ((color >> 16 & 255) / 255.0F);
        FLOAT_BUFFER[1] = ((color >> 8 & 255) / 255.0F);
        FLOAT_BUFFER[2] = ((color & 255) / 255.0F);
        FLOAT_BUFFER[3] = ((color >> 24 & 255) / 255.0F);

        return FLOAT_BUFFER;
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#RGBtoHSB(int, int, int, float[])} instead
     */
    @Deprecated(forRemoval = true)
    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        return org.betterx.ui.ColorUtil.RGBtoHSB(r, g, b, hsbvals);
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#HSBtoRGB(float, float, float)} instead
     */
    @Deprecated(forRemoval = true)
    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        return org.betterx.ui.ColorUtil.HSBtoRGB(hue, saturation, brightness);
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#parseHex(String)} instead
     */
    @Deprecated(forRemoval = true)
    public static int parseHex(String hexColor) {
        return org.betterx.ui.ColorUtil.parseHex(hexColor);
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#toABGR(int)} instead
     */
    @Deprecated(forRemoval = true)
    public static int toABGR(int color) {
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        return 0xFF000000 | b << 16 | g << 8 | r;
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#ABGRtoARGB(int)} instead
     */
    @Deprecated(forRemoval = true)
    public static int ABGRtoARGB(int color) {
        int a = (color >> 24) & 255;
        int b = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int r = color & 255;
        return a << 24 | r << 16 | g << 8 | b;
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#colorBrigtness(int, float)} instead
     */
    @Deprecated(forRemoval = true)
    public static int colorBrigtness(int color, float val) {
        RGBtoHSB((color >> 16) & 255, (color >> 8) & 255, color & 255, FLOAT_BUFFER);
        FLOAT_BUFFER[2] += val / 10.0F;
        FLOAT_BUFFER[2] = Mth.clamp(FLOAT_BUFFER[2], 0.0F, 1.0F);
        return HSBtoRGB(FLOAT_BUFFER[0], FLOAT_BUFFER[1], FLOAT_BUFFER[2]);
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#applyTint(int, int)} instead
     */
    @Deprecated(forRemoval = true)
    public static int applyTint(int color, int tint) {
        return colorBrigtness(ColorHelper.multiplyColor(color, tint), 1.5F);
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#colorDistance(int, int)} instead
     */
    @Deprecated(forRemoval = true)
    public static int colorDistance(int color1, int color2) {
        int r1 = (color1 >> 16) & 255;
        int g1 = (color1 >> 8) & 255;
        int b1 = color1 & 255;
        int r2 = (color2 >> 16) & 255;
        int g2 = (color2 >> 8) & 255;
        int b2 = color2 & 255;
        return MHelper.sqr(r1 - r2) + MHelper.sqr(g1 - g2) + MHelper.sqr(b1 - b2);
    }

    private static final Map<ResourceLocation, Integer> colorPalette = Maps.newHashMap();

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#extractColor(Item)} instead
     */
    @Deprecated(forRemoval = true)
    @Environment(EnvType.CLIENT)
    public static int extractColor(Item item) {
        return org.betterx.ui.ColorUtil.extractColor(item);
    }

    /**
     * @deprecated Please use {@link org.betterx.ui.ColorUtil#loadImage(ResourceLocation, int, int)} instead
     */
    @Deprecated(forRemoval = true)
    @Environment(EnvType.CLIENT)
    public static NativeImage loadImage(ResourceLocation image, int w, int h) {
        return org.betterx.ui.ColorUtil.loadImage(image, w, h);
    }
}