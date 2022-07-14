package org.betterx.ui.layout.components;


import org.betterx.ui.layout.components.input.MouseEvent;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;

public class Panel implements ComponentWithBounds, GuiEventListener {
    protected Component<?> child;
    public final Rectangle bounds;

    public Panel(int width, int height) {
        bounds = new Rectangle(0, 0, width, height);
    }

    public void setChild(Component<?> c) {
        this.child = c;
    }

    public boolean mouseEvent(MouseEvent event, int x, int y) {
        if (child != null) {
            return child.mouseEvent(event, x - bounds.left, y - bounds.top);
        }
        return false;
    }

    public void calculateLayout() {
        if (child != null) {
            child.updateContainerWidth(bounds.width);
            child.updateContainerHeight(bounds.height);
            child.setRelativeBounds(0, 0);
        }
    }

    public void render(PoseStack poseStack) {
        if (child != null) {
            child.render(poseStack, bounds, bounds);
        }
    }

    @Override
    public Rectangle getRelativeBounds() {
        return bounds;
    }

    @Override
    public void mouseMoved(double x, double y) {
        if (child != null)
            child.mouseMoved(x - bounds.left, y - bounds.top);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (child != null)
            return child.mouseClicked(x - bounds.left, y - bounds.top, button);
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (child != null)
            return child.mouseReleased(x - bounds.left, y - bounds.top, button);
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double x2, double y2) {
        if (child != null)
            return child.mouseDragged(x - bounds.left, y - bounds.top, button, x2 - bounds.left, y2 - bounds.top);
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double f) {
        if (child != null)
            return child.mouseScrolled(x - bounds.left, y - bounds.top, f);
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (child != null)
            return child.keyPressed(i, j, k);
        return false;
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (child != null)
            return child.keyReleased(i, j, k);
        return false;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (child != null)
            return child.charTyped(c, i);
        return false;
    }

    @Override
    public boolean changeFocus(boolean bl) {
        if (child != null)
            return child.changeFocus(bl);
        return false;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        if (child != null)
            return child.isMouseOver(x, y);
        return false;
    }
}
