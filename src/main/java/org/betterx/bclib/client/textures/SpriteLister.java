package org.betterx.bclib.client.textures;

import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value = EnvType.CLIENT)
public class SpriteLister extends DirectoryLister {
    public SpriteLister(String string) {
        super(string, string + "/");
    }
}
