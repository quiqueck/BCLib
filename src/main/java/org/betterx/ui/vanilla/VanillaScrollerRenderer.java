package org.betterx.ui.vanilla;

import org.betterx.ui.layout.components.render.ScrollerRenderer;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;

public class VanillaScrollerRenderer implements ScrollerRenderer {
    public static final VanillaScrollerRenderer DEFAULT = new VanillaScrollerRenderer();

    @Override
    public void renderScrollBar(Rectangle b, int pickerOffset, int pickerSize) {
        b = this.getScrollerBounds(b);
        Rectangle p = this.getPickerBounds(b, pickerOffset, pickerSize);


        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);


        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        //scroller background
        bufferBuilder.vertex(b.left, b.bottom(), 0.0D).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(b.right(), b.bottom(), 0.0D).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(b.right(), b.top, 0.0D).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(b.left, b.top, 0.0D).color(0, 0, 0, 255).endVertex();

        //scroll widget shadow
        bufferBuilder.vertex(p.left, p.bottom(), 0.0D).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(p.right(), p.bottom(), 0.0D).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(p.right(), p.top, 0.0D).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(p.left, p.top, 0.0D).color(128, 128, 128, 255).endVertex();

        //scroll widget
        bufferBuilder.vertex(p.left, p.bottom() - 1, 0.0D)
                     .color(192, 192, 192, 255)
                     .endVertex();
        bufferBuilder.vertex(p.right() - 1, p.bottom() - 1, 0.0D)
                     .color(192, 192, 192, 255)
                     .endVertex();
        bufferBuilder.vertex(p.right() - 1, p.top, 0.0D).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(p.left, p.top, 0.0D).color(192, 192, 192, 255).endVertex();
        
        tesselator.end();
    }
}
