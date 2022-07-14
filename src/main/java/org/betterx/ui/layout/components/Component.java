package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.input.MouseEvent;
import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.values.Alignment;
import org.betterx.ui.layout.values.DynamicSize;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;

public abstract class Component<R extends ComponentRenderer> implements ComponentWithBounds, GuiEventListener {
    protected final R renderer;
    protected final DynamicSize width;
    protected final DynamicSize height;
    protected Rectangle relativeBounds;
    protected Alignment vAlign = Alignment.MIN;
    protected Alignment hAlign = Alignment.MIN;

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
        onBoundsChanged();
    }

    protected void onBoundsChanged() {
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

    public void render(PoseStack poseStack, Rectangle parentBounds, Rectangle clipRect) {
        Rectangle r = relativeBounds.movedBy(parentBounds.left, parentBounds.top);
        Rectangle clip = r.intersect(clipRect);
        poseStack.pushPose();
        poseStack.translate(relativeBounds.left, relativeBounds.top, 0);
        if (r.overlaps(clip)) {
            renderInBounds(poseStack, r, clip);
        }
        poseStack.popPose();
    }

    protected void renderInBounds(PoseStack poseStack, Rectangle renderBounds, Rectangle clipRect) {
        if (renderer != null) {
            renderer.renderInBounds(poseStack, renderBounds, clipRect);
        }
    }

    boolean mouseEvent(MouseEvent event, int x, int y) {
        return onMouseEvent(event, x, y);
    }

    public boolean onMouseEvent(MouseEvent event, int x, int y) {
        return false;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + relativeBounds + ", " + width.calculatedSize() + "x" + height.calculatedSize() + ")";
    }

    public Component<R> alignTop() {
        vAlign = Alignment.MIN;
        return this;
    }

    public Component<R> alignBottom() {
        vAlign = Alignment.MAX;
        return this;
    }

    public Component<R> centerVertical() {
        vAlign = Alignment.CENTER;
        return this;
    }

    public Component<R> alignLeft() {
        hAlign = Alignment.MIN;
        return this;
    }

    public Component<R> alignRight() {
        hAlign = Alignment.MAX;
        return this;
    }

    public Component<R> centerHorizontal() {
        hAlign = Alignment.CENTER;
        return this;
    }
}
