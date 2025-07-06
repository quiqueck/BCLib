package org.betterx.bclib.registry;

import org.betterx.bclib.furniture.renderer.RenderChair;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;


@Environment(EnvType.CLIENT)
public class BaseBlockEntityRenders {
    public static void register() {
        EntityRendererRegistry.register(BaseBlockEntities.CHAIR, RenderChair::new);
    }
}
