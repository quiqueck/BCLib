package org.betterx.ui.layout.components.render;

import org.betterx.ui.ColorUtil;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;

public class RenderHelper {
    public static void outline(PoseStack poseStack, int x0, int y0, int x1, int y1, int color) {
        outline(poseStack, x0, y0, x1, y1, color, color);
    }

    public static void outline(PoseStack poseStack, int x0, int y0, int x1, int y1, int color1, int color2) {
        int n;
        if (x1 < x0) {
            n = x0;
            x0 = x1;
            x1 = n;
        }

        if (y1 < y0) {
            n = y0;
            y0 = y1;
            y1 = n;
        }
        y1--;
        x1--;

        Matrix4f transform = poseStack.last().pose();
        innerHLine(transform, x0, x1, y0, color1);
        innerVLine(transform, x0, y0 + 1, y1, color1);
        innerHLine(transform, x0 + 1, x1, y1, color1);
        innerVLine(transform, x1, y0 + 1, y1 - 1, color2);
    }

    public static void hLine(PoseStack poseStack, int x0, int x1, int y, int color) {
        if (x1 < x0) {
            int m = x0;
            x0 = x1;
            x1 = m;
        }

        innerHLine(poseStack.last().pose(), x0, x1, y, color);
    }

    protected static void innerHLine(Matrix4f transform, int x0, int x1, int y, int color) {
        innerFill(transform, x0, y, x1 + 1, y + 1, color);
    }

    protected static void vLine(PoseStack poseStack, int x, int y0, int y1, int color) {
        if (y1 < y0) {
            int m = y0;
            y0 = y1;
            y1 = m;
        }
        innerVLine(poseStack.last().pose(), x, y0, y1, color);
    }

    protected static void innerVLine(Matrix4f transform, int x, int y0, int y1, int color) {
        innerFill(transform, x, y0, x + 1, y1 + 1, color);
    }

    private static void innerFill(Matrix4f transform, int x0, int y0, int x1, int y1, int color) {
        float[] cl = ColorUtil.toFloatArray(color);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(transform, (float) x0, (float) y1, 0.0F).color(cl[0], cl[1], cl[2], cl[3]).endVertex();
        bufferBuilder.vertex(transform, (float) x1, (float) y1, 0.0F).color(cl[0], cl[1], cl[2], cl[3]).endVertex();
        bufferBuilder.vertex(transform, (float) x1, (float) y0, 0.0F).color(cl[0], cl[1], cl[2], cl[3]).endVertex();
        bufferBuilder.vertex(transform, (float) x0, (float) y0, 0.0F).color(cl[0], cl[1], cl[2], cl[3]).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
