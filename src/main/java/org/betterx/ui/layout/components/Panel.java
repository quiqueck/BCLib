package org.betterx.ui.layout.components;


import org.betterx.ui.layout.components.input.MouseEvent;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;

public class Panel implements ComponentWithBounds {
    protected Component<?> child;
    public final Rectangle bounds;

    public Panel(int width, int height) {
        bounds = new Rectangle(0, 0, width, height);
    }

    public void setChild(Component<?> c) {
        this.child = c;
    }

    public void mouseEvent(MouseEvent event, int x, int y) {
        if (child != null) {
            child.mouseEvent(event, x - bounds.left, y - bounds.top);
        }
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
}
