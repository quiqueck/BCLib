package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColorUtil {
    private static final float[] FLOAT_BUFFER = new float[4];
    private static final int ALPHA = 255 << 24;

    public static int color(int r, int g, int b) {
        return ALPHA | (r << 16) | (g << 8) | b;
    }

    public static int color(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return color(r, g, b);
    }

    public static int[] toIntArray(int color) {
        return new int[]{(color >> 24) & 255, (color >> 16) & 255, (color >> 8) & 255, color & 255};
    }

    public static float[] toFloatArray(int color) {
        FLOAT_BUFFER[0] = ((color >> 16 & 255) / 255.0F);
        FLOAT_BUFFER[1] = ((color >> 8 & 255) / 255.0F);
        FLOAT_BUFFER[2] = ((color & 255) / 255.0F);
        FLOAT_BUFFER[3] = ((color >> 24 & 255) / 255.0F);

        return FLOAT_BUFFER;
    }

    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue, saturation, brightness;
        if (hsbvals == null) {
            hsbvals = FLOAT_BUFFER;
        }
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        brightness = ((float) cmax) / 255.0F;
        if (cmax != 0) saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else saturation = 0;
        if (saturation == 0) hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax) hue = bluec - greenc;
            else if (g == cmax) hue = 2.0F + redc - bluec;
            else hue = 4.0F + greenc - redc;
            hue = hue / 6.0F;
            if (hue < 0) hue = hue + 1.0F;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0F + 0.5F);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0F;
            float f = h - (float) java.lang.Math.floor(h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - (saturation * (1.0F - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (t * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                    break;
                case 1:
                    r = (int) (q * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (p * 255.0F + 0.5F);
                    break;
                case 2:
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (brightness * 255.0F + 0.5F);
                    b = (int) (t * 255.0F + 0.5F);
                    break;
                case 3:
                    r = (int) (p * 255.0F + 0.5F);
                    g = (int) (q * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                    break;
                case 4:
                    r = (int) (t * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (brightness * 255.0F + 0.5F);
                    break;
                case 5:
                    r = (int) (brightness * 255.0F + 0.5F);
                    g = (int) (p * 255.0F + 0.5F);
                    b = (int) (q * 255.0F + 0.5F);
                    break;
            }
        }
        return 0xFF000000 | (r << 16) | (g << 8) | (b << 0);
    }

    public static int parseHex(String hexColor) {
        int len = hexColor.length();
        if (len < 6 || len > 8 || len % 2 > 0) {
            return -1;
        }

        int color, shift;
        if (len == 6) {
            color = 0xFF000000;
            shift = 16;
        } else {
            color = 0;
            shift = 24;
        }

        try {
            String[] splited = hexColor.split("(?<=\\G.{2})");
            for (String digit : splited) {
                color |= Integer.valueOf(digit, 16) << shift;
                shift -= 8;
            }
        } catch (NumberFormatException ex) {
            BCLib.LOGGER.catching(ex);
            return -1;
        }

        return color;
    }

    public static int toABGR(int color) {
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        return 0xFF000000 | b << 16 | g << 8 | r;
    }

    public static int ABGRtoARGB(int color) {
        int a = (color >> 24) & 255;
        int b = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int r = color & 255;
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int colorBrigtness(int color, float val) {
        RGBtoHSB((color >> 16) & 255, (color >> 8) & 255, color & 255, FLOAT_BUFFER);
        FLOAT_BUFFER[2] += val / 10.0F;
        FLOAT_BUFFER[2] = Mth.clamp(FLOAT_BUFFER[2], 0.0F, 1.0F);
        return HSBtoRGB(FLOAT_BUFFER[0], FLOAT_BUFFER[1], FLOAT_BUFFER[2]);
    }

    public static int applyTint(int color, int tint) {
        return colorBrigtness(ColorHelper.multiplyColor(color, tint), 1.5F);
    }

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

    @Environment(EnvType.CLIENT)
    public static int extractColor(Item item) {
        ResourceLocation id = Registry.ITEM.getKey(item);
        if (id.equals(Registry.ITEM.getDefaultKey())) return -1;
        if (colorPalette.containsKey(id)) {
            return colorPalette.get(id);
        }
        ResourceLocation texture;
        if (item instanceof BlockItem) {
            texture = new ResourceLocation(id.getNamespace(), "textures/block/" + id.getPath() + ".png");
        } else {
            texture = new ResourceLocation(id.getNamespace(), "textures/item/" + id.getPath() + ".png");
        }
        NativeImage image = loadImage(texture, 16, 16);
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < 16; j++) {
                int col = image.getPixelRGBA(i, j);
                if (((col >> 24) & 255) > 0) {
                    colors.add(ABGRtoARGB(col));
                }
            }
        }
        image.close();

        if (colors.size() == 0) return -1;

        ColorExtractor extractor = new ColorExtractor(colors);
        int color = extractor.analize();
        colorPalette.put(id, color);

        return color;
    }

    @Environment(EnvType.CLIENT)
    public static NativeImage loadImage(ResourceLocation image, int w, int h) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceManager resourceManager = minecraft.getResourceManager();
        var imgResource = resourceManager.getResource(image);
        if (imgResource.isPresent()) {
            try {
                return NativeImage.read(imgResource.get().open());
            } catch (IOException e) {
                BCLib.LOGGER.warning("Can't load texture image: {}. Will be created empty image.", image);
                BCLib.LOGGER.warning("Cause: {}.", e.getMessage());
            }
        }
        return new NativeImage(w, h, false);
    }
}