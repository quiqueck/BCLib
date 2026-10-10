package org.betterx.bclib.client;

import de.ambertation.wunderlib.utils.ColorUtilARGB32;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.ColorExtractor;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Color helpers that read textures and therefore only work on the client. Everything else lives in
 * {@link ColorUtilARGB32}.
 */
@Environment(EnvType.CLIENT)
public class ClientColorUtil {
    private static final Map<ResourceLocation, Integer> colorPalette = Maps.newHashMap();

    public static int extractColor(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (id.equals(BuiltInRegistries.ITEM.getDefaultKey())) return -1;
        if (colorPalette.containsKey(id)) {
            return colorPalette.get(id);
        }
        ResourceLocation texture;
        if (item instanceof BlockItem) {
            texture = ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(),
                    "textures/block/" + id.getPath() + ".png"
            );
        } else {
            texture = ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(),
                    "textures/item/" + id.getPath() + ".png"
            );
        }
        NativeImage image = loadImage(texture, 16, 16);
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < 16; j++) {
                //getPixel already returns ARGB (getPixelABGR is the native order)
                int col = image.getPixel(i, j);
                if (((col >> 24) & 255) > 0) {
                    colors.add(col);
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

    public static NativeImage loadImage(ResourceLocation image, int w, int h) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceManager resourceManager = minecraft.getResourceManager();
        var imgResource = resourceManager.getResource(image);
        if (imgResource.isPresent()) {
            try {
                return NativeImage.read(imgResource.get().open());
            } catch (IOException e) {
                BCLib.LOGGER.warn("Can't load texture image: {}. Will be created empty image.", image);
                BCLib.LOGGER.warn("Cause: {}.", e.getMessage());
            }
        }
        return new NativeImage(w, h, false);
    }
}
