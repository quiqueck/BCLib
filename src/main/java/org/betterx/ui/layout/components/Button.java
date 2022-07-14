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
            if (event == MouseEvent.DOWN) return vanillaButton.mouseClicked(x, y, 0);

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

    @Override
    public void mouseMoved(double x, double y) {
        if (vanillaButton != null)
            vanillaButton.mouseMoved(x - relativeBounds.left, y - relativeBounds.top);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (vanillaButton != null)
            return vanillaButton.mouseClicked(x - relativeBounds.left, y - relativeBounds.top, button);
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (vanillaButton != null)
            return vanillaButton.mouseReleased(x - relativeBounds.left, y - relativeBounds.top, button);
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double x2, double y2) {
        if (vanillaButton != null)
            return vanillaButton.mouseDragged(
                    x - relativeBounds.left,
                    y - relativeBounds.top,
                    button,
                    x2 - relativeBounds.left,
                    y2 - relativeBounds.top
            );
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double f) {
        if (vanillaButton != null)
            return vanillaButton.mouseScrolled(x - relativeBounds.left, y - relativeBounds.top, f);
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (vanillaButton != null)
            return vanillaButton.keyPressed(i, j, k);
        return false;
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (vanillaButton != null)
            return vanillaButton.keyReleased(i, j, k);
        return false;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (vanillaButton != null)
            return vanillaButton.charTyped(c, i);
        return false;
    }

    @Override
    public boolean changeFocus(boolean bl) {
        if (vanillaButton != null)
            return vanillaButton.changeFocus(bl);
        return false;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        if (vanillaButton != null)
            return vanillaButton.isMouseOver(x, y);
        return false;
    }

}
