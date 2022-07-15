package org.betterx.ui.layout.components.render;

import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NullRenderer implements ComponentRenderer {
    @Override
    public void renderInBounds(PoseStack stack, int x, int y, float a, Rectangle bounds, Rectangle clipRect) {
        
    }
}
