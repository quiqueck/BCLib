package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AbstractVanillaComponentRenderer<C extends AbstractWidget, V extends AbstractVanillaComponent<C, V>> implements ComponentRenderer {
    V linkedComponent;

    public Font getFont() {
        return Minecraft.getInstance().font;
    }

    public int getWidth(net.minecraft.network.chat.Component c) {
        return getFont().width(c.getVisualOrderText()) + 24;
    }

    public int getHeight(net.minecraft.network.chat.Component c) {
        return getFont().lineHeight + 11;
    }

    protected V getLinkedComponent() {
        return linkedComponent;
    }

    @Override
    public void renderInBounds(PoseStack poseStack, int x, int y, float a, Rectangle bounds, Rectangle clipRect) {
        if (linkedComponent != null) {
            if (linkedComponent.vanillaComponent != null) {
                linkedComponent.vanillaComponent.render(poseStack, x, y, a);
            }

        }
    }
}
