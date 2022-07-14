package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.input.MouseEvent;
import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.values.DynamicSize;
import org.betterx.ui.layout.values.Rectangle;

public abstract class Component<R extends ComponentRenderer> implements ComponentWithBounds {
    protected final R renderer;
    protected final DynamicSize width;
    protected final DynamicSize height;
    protected Rectangle relativeBounds;

    public Component(DynamicSize width, DynamicSize height, R renderer) {
        this.width = width.attachComponent(this::getContentWidth);
        this.height = height.attachComponent(this::getContentHeight);
        this.renderer = renderer;
    }

    protected int updateContainerWidth(int containerWidth) {
        return width.setCalculatedSize(containerWidth);
    }

    protected int updateContainerHeight(int containerHeight) {
        return height.setCalculatedSize(containerHeight);
    }

    void setRelativeBounds(int left, int top) {
        relativeBounds = new Rectangle(left, top, width.calculatedSize(), height.calculatedSize());
    }

    public Rectangle getRelativeBounds() {
        return relativeBounds;
    }

    public abstract int getContentWidth();
    public abstract int getContentHeight();

    public int fillWidth(int parentSize, int fillSize) {
        return width.fill(fillSize);
    }

    public int fillHeight(int parentSize, int fillSize) {
        return height.fill(fillSize);
    }

    public int getWidth() {
        return width.calculatedSize();
    }

    public int getHeight() {
        return height.calculatedSize();
    }

    public void render(Rectangle parentBounds, Rectangle clipRect) {
        Rectangle r = relativeBounds.movedBy(parentBounds.left, parentBounds.top);
        Rectangle clip = r.intersect(clipRect);
        if (r.overlaps(clip)) {
            renderInBounds(r, clip);
        }
    }

    protected void renderInBounds(Rectangle renderBounds, Rectangle clipRect) {
        if (renderer != null) {
            renderer.renderInBounds(renderBounds, clipRect);
        }
    }

    void mouseEvent(MouseEvent event, int x, int y) {
        onMouseEvent(event, x, y);
    }

    public boolean onMouseEvent(MouseEvent event, int x, int y) {
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + relativeBounds + ", " + width.calculatedSize() + "x" + height.calculatedSize() + ")";
    }
}
