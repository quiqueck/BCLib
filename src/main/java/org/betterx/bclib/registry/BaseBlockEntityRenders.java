package org.betterx.bclib.registry;

import org.betterx.bclib.client.render.BaseChestBlockEntityRenderer;

import net.minecraft.client.renderer.blockentity.SignRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class BaseBlockEntityRenders {
    public static void register() {
        BlockEntityRendererRegistry.register(BaseBlockEntities.CHEST, BaseChestBlockEntityRenderer::new);

        //make sure we can lod signs from older worlds. Can be removed in the future
        BlockEntityRendererRegistry.register(BaseBlockEntities.SIGN, SignRenderer::new);
    }
}
