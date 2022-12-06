package org.betterx.bclib.mixin.client;

import org.betterx.bclib.client.textures.AtlasSetManager;

import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(SpriteResourceLoader.class)
public class AtlasSetMixin {
    @ModifyVariable(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/atlas/SpriteResourceLoader;<init>(Ljava/util/List;)V"))
    private static List<SpriteSource> bcl_load(
            List<SpriteSource> list,
            ResourceManager resourceManager,
            ResourceLocation type
    ) {
        AtlasSetManager.onLoadResources(type, list);
        return list;
    }
}
