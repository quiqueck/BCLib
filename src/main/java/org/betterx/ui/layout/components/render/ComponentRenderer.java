package org.betterx.ui.layout.components.render;

import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;

public interface ComponentRenderer {
    void renderInBounds(PoseStack stack, int x, int y, float a, Rectangle bounds, Rectangle clipRect);
}
