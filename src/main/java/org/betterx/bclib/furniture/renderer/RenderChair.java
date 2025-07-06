package org.betterx.bclib.furniture.renderer;

import org.betterx.bclib.furniture.entity.EntityChair;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
class ChairRenderState extends EntityRenderState {

}


@Environment(value = EnvType.CLIENT)
public class RenderChair extends EntityRenderer<EntityChair, ChairRenderState> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");

    public RenderChair(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ChairRenderState createRenderState() {
        return new ChairRenderState();
    }
}
