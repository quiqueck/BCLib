package org.betterx.bclib.client.render;

import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.Sets;

import java.util.Set;

public class EmissiveTextureInfo {
    private static final Set<ResourceLocation> EMISSIVE_TEXTURES = Sets.newHashSet();
    private static final Set<ResourceLocation> EMISSIVE_BLOCKS = Sets.newHashSet();

    public static void clear() {
        EMISSIVE_TEXTURES.clear();
        EMISSIVE_BLOCKS.clear();
    }

    public static void addTexture(ResourceLocation texture) {
        EMISSIVE_TEXTURES.add(texture);
    }

    public static void addBlock(ResourceLocation blockID) {
        EMISSIVE_BLOCKS.add(blockID);
    }

    public static boolean isEmissiveTexture(ResourceLocation texture) {
        return EMISSIVE_TEXTURES.contains(texture);
    }

    public static boolean isEmissiveBlock(ResourceLocation blockID) {
        return EMISSIVE_BLOCKS.contains(blockID);
    }
}
