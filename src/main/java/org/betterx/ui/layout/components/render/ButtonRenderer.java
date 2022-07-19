package org.betterx.ui.layout.components.render;

import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.components.AbstractVanillaComponentRenderer;
import org.betterx.ui.layout.components.Button;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ButtonRenderer extends AbstractVanillaComponentRenderer<net.minecraft.client.gui.components.Button, Button> {

    @Override
    public void renderInBounds(
            PoseStack poseStack,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    ) {
        super.renderInBounds(poseStack, mouseX, mouseY, deltaTicks, bounds, clipRect);
        if (getLinkedComponent() != null && getLinkedComponent().isGlowing()) {
            RenderHelper.outline(poseStack, 0, 0, bounds.width, bounds.height, ColorUtil.RED);
        }
    }
}
