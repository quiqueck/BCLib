package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.input.MouseEvent;
import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.values.DynamicSize;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

class ButtonRenderer implements ComponentRenderer {
    Button linkedButton;

    public Font getFont() {
        return Minecraft.getInstance().font;
    }

    public int getWidth(net.minecraft.network.chat.Component c) {
        return getFont().width(c.getVisualOrderText()) + 24;
    }

    public int getHeight(net.minecraft.network.chat.Component c) {
        return getFont().lineHeight + 12;
    }

    @Override
    public void renderInBounds(PoseStack poseStack, Rectangle bounds, Rectangle clipRect) {
        if (linkedButton != null) {
            if (linkedButton.vanillaButton != null) {
                linkedButton.vanillaButton.render(poseStack, linkedButton.mouseX, linkedButton.mouseY, 1);
            }

        }
    }
}

public class Button extends Component<ButtonRenderer> {
    int mouseX, mouseY;
    final net.minecraft.network.chat.Component component;
    final net.minecraft.client.gui.components.Button.OnPress onPress;
    final net.minecraft.client.gui.components.Button.OnTooltip onTooltip;
    net.minecraft.client.gui.components.Button vanillaButton;

    public Button(
            DynamicSize width,
            DynamicSize height,
            net.minecraft.network.chat.Component component,
            net.minecraft.client.gui.components.Button.OnPress onPress,
            net.minecraft.client.gui.components.Button.OnTooltip onTooltip
    ) {
        super(width, height, new ButtonRenderer());
        renderer.linkedButton = this;

        this.component = component;
        this.onPress = onPress;
        this.onTooltip = onTooltip;
    }

    @Override
    public boolean onMouseEvent(MouseEvent event, int x, int y) {
        mouseX = x;
        mouseY = y;
        if (vanillaButton != null && relativeBounds.contains(x, y)) {


            return true;
        }
        return super.onMouseEvent(event, x, y);
    }

    @Override
    protected void onBoundsChanged() {
        vanillaButton = new net.minecraft.client.gui.components.Button(
                0,
                0,
                relativeBounds.width,
                relativeBounds.height,
                component,
                onPress,
                onTooltip
        );
    }

    @Override
    public int getContentWidth() {
        return renderer.getWidth(component);
    }

    @Override
    public int getContentHeight() {
        return renderer.getHeight(component);
    }
}
