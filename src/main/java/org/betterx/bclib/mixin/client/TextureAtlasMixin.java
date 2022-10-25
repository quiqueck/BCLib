package org.betterx.bclib.mixin.client;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.render.EmissiveTextureInfo;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;

import net.fabricmc.loader.api.FabricLoader;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;

@Mixin(SpriteLoader.class)
public class TextureAtlasMixin {
    @Shadow
    @Final
    private ResourceLocation location;
    private static final int EMISSIVE_ALPHA = 254 << 24;
    private boolean bclib_modifyAtlas;
    private static ResourceManager bclib_resourceManager;

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void bclib_onAtlasInit(
            ResourceLocation textureAtlasLocation,
            int maxSupportedTextureSize,
            CallbackInfo ci
    ) {

        boolean hasOptifine = FabricLoader.getInstance().isModLoaded("optifabric");
        bclib_modifyAtlas = !hasOptifine && textureAtlasLocation.toString()
                                                                .equals("minecraft:textures/atlas/blocks.png");
        if (bclib_modifyAtlas) {
            EmissiveTextureInfo.clear();
        }
    }

    @Inject(method = "listSprites(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/lang/String;Ljava/lang/String;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private static void bclib_listSprites(
            ResourceManager resourceManager,
            String string,
            String string2,
            BiConsumer<ResourceLocation, Resource> biConsumer,
            CallbackInfo ci
    ) {
        bclib_resourceManager = resourceManager;
    }

    @Inject(method = "loadSprite", at = @At("HEAD"), cancellable = true)
    private void bclib_loadSprite(
            ResourceLocation location, Resource resource, CallbackInfoReturnable<SpriteContents> cir
    ) {
        if (!bclib_modifyAtlas || bclib_resourceManager == null) {
            return;
        }

        if (!location.getPath().startsWith("block")) {
            return;
        }

        AnimationMetadataSection animationMetadataSection;
        try {
            animationMetadataSection = resource.metadata()
                                               .getSection(AnimationMetadataSection.SERIALIZER)
                                               .orElse(AnimationMetadataSection.EMPTY);
        } catch (Exception exception) {
            BCLib.LOGGER.error("Unable to parse metadata from {} : {}", (Object) this.location, (Object) exception);
            cir.setReturnValue(null);
            cir.cancel();
            return;
        }

        ResourceLocation emissiveLocation = new ResourceLocation(
                location.getNamespace(),
                "textures/" + location.getPath() + "_e.png"
        );
        Optional<Resource> emissiveRes = bclib_resourceManager.getResource(emissiveLocation);
        if (emissiveRes.isPresent()) {
            NativeImage sprite = null;
            NativeImage emission = null;
            try {
//                ResourceLocation spriteLocation = new ResourceLocation(
//                        location.getNamespace(),
//                        "textures/" + location.getPath() + ".png"
//                );
                //Resource resource = resourceManager.getResource(spriteLocation).orElse(null);
                sprite = NativeImage.read(resource.open());

                resource = emissiveRes.get();
                emission = NativeImage.read(resource.open());
            } catch (IOException e) {
                BCLib.LOGGER.warning(e.getMessage());
            }
            if (sprite != null && emission != null) {
                int width = Math.min(sprite.getWidth(), emission.getWidth());
                int height = Math.min(sprite.getHeight(), emission.getHeight());
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int argb = emission.getPixelRGBA(x, y);
                        int alpha = (argb >> 24) & 255;
                        if (alpha > 127) {
                            int r = (argb >> 16) & 255;
                            int g = (argb >> 8) & 255;
                            int b = argb & 255;
                            if (r > 0 || g > 0 || b > 0) {
                                argb = (argb & 0x00FFFFFF) | EMISSIVE_ALPHA;
                                sprite.setPixelRGBA(x, y, argb);
                            }
                        }
                    }
                }

                FrameSize frameSize = animationMetadataSection.calculateFrameSize(
                        sprite.getWidth(),
                        sprite.getHeight()
                );
                if (!Mth.isDivisionInteger(
                        sprite.getWidth(),
                        frameSize.width()
                ) || !Mth.isDivisionInteger(sprite.getHeight(), frameSize.height())) {
                    BCLib.LOGGER.error(
                            "Image {} size {},{} is not multiple of frame size {},{}",
                            this.location,
                            sprite.getWidth(),
                            sprite.getHeight(),
                            frameSize.width(),
                            frameSize.height()
                    );
                    sprite.close();
                    cir.setReturnValue(null);
                    cir.cancel();
                }

                SpriteContents result = new SpriteContents(location, frameSize, sprite, animationMetadataSection);

                EmissiveTextureInfo.addTexture(location);
                cir.setReturnValue(result);
            }
        }
    }
}
