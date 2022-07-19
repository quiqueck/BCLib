package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.components.render.NullRenderer;
import org.betterx.ui.layout.components.render.ScrollerRenderer;
import org.betterx.ui.layout.values.Alignment;
import org.betterx.ui.layout.values.Rectangle;
import org.betterx.ui.layout.values.Value;
import org.betterx.ui.vanilla.VanillaScrollerRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class VerticalScroll<R extends ComponentRenderer, RS extends ScrollerRenderer> extends LayoutComponent<R, VerticalScroll<R, RS>> implements ContainerEventHandler {
    protected LayoutComponent<?, ?> child;
    protected final RS scrollerRenderer;
    protected Rectangle viewBounds;

    protected int dist;
    protected double scrollerY;
    protected int scrollerHeight;
    protected int travel;
    protected int topOffset;

    public VerticalScroll(Value width, Value height, RS scrollerRenderer) {
        this(width, height, scrollerRenderer, null);
    }

    public VerticalScroll(Value width, Value height, RS scrollerRenderer, R renderer) {
        super(width, height, renderer);
        this.scrollerRenderer = scrollerRenderer;
    }

    public static VerticalScroll<NullRenderer, VanillaScrollerRenderer> create(LayoutComponent<?, ?> c) {
        return create(Value.relative(1), Value.relative(1), c);
    }

    public static VerticalScroll<NullRenderer, VanillaScrollerRenderer> create(
            Value width,
            Value height,
            LayoutComponent<?, ?> c
    ) {
        VerticalScroll<NullRenderer, VanillaScrollerRenderer> res = new VerticalScroll<>(
                width,
                height,
                VanillaScrollerRenderer.DEFAULT,
                null
        );
        res.setChild(c);
        return res;
    }

    List<LayoutComponent<?, ?>> children = List.of();

    public void setChild(LayoutComponent<?, ?> c) {
        this.child = c;
        children = List.of(child);
    }

    @Override
    protected int updateContainerWidth(int containerWidth) {
        int myWidth = width.calculateOrFill(containerWidth);
        if (child != null) {
            child.width.calculateOrFill(myWidth - scrollerWidth());
            child.updateContainerWidth(child.width.calculatedSize());
        }
        return myWidth;
    }

    @Override
    protected int updateContainerHeight(int containerHeight) {
        int myHeight = height.calculateOrFill(containerHeight);
        if (child != null) {
            child.height.calculateOrFill(myHeight);
            child.updateContainerHeight(child.height.calculatedSize());
        }
        return myHeight;
    }

    protected int scrollerWidth() {
        return scrollerRenderer.scrollerWidth() + scrollerRenderer.scrollerPadding();
    }

    @Override
    public int getContentWidth() {
        return scrollerWidth() + (child != null
                ? child.getContentWidth()
                : 0);
    }

    @Override
    public int getContentHeight() {
        return child != null ? child.getContentHeight() : 0;
    }

    @Override
    void setRelativeBounds(int left, int top) {
        super.setRelativeBounds(left, top);

        if (child != null) {
            int width = relativeBounds.width;
            boolean willNeedScrollBar = child.height.calculatedSize() > relativeBounds.height;
            if (willNeedScrollBar) width -= scrollerWidth();
            int childTop = width - child.width.calculatedSize();
            if (child.hAlign == Alignment.MIN) childTop = 0;
            else if (child.hAlign == Alignment.CENTER) childTop /= 2;

            int childLeft = relativeBounds.height - child.height.calculatedSize();
            if (child.vAlign == Alignment.MIN) childLeft = 0;
            else if (child.vAlign == Alignment.CENTER) childLeft /= 2;

            child.setRelativeBounds(childLeft, childTop);
        }

        updateScrollViewMetrics();
    }

    @Override
    protected void renderInBounds(
            PoseStack poseStack,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle renderBounds,
            Rectangle clipRect
    ) {
        super.renderInBounds(poseStack, mouseX, mouseY, deltaTicks, renderBounds, clipRect);

        if (showScrollBar()) {
            if (child != null) {
                poseStack.pushPose();
                poseStack.translate(0, scrollerOffset(), 0);
                setClippingRect(clipRect);
                child.render(
                        poseStack, mouseX, mouseY - scrollerOffset(), deltaTicks,
                        renderBounds.movedBy(0, scrollerOffset(), scrollerWidth(), 0),
                        clipRect
                );
                setClippingRect(null);
                poseStack.popPose();
            }
            scrollerRenderer.renderScrollBar(renderBounds, saveScrollerY(), scrollerHeight);
        } else {
            if (child != null) {
                child.render(poseStack, mouseX, mouseY, deltaTicks, renderBounds, clipRect);
            }
        }
    }

    private boolean mouseDown = false;
    private int mouseDownY = 0;
    private int scrollerDownY = 0;

    protected void updateScrollViewMetrics() {
        final int view = relativeBounds.height;
        final int content = child.relativeBounds.height;

        this.dist = content - view;
        this.scrollerHeight = Math.max(scrollerRenderer.scrollerHeight(), (view * view) / content);
        this.travel = view - this.scrollerHeight;
        this.topOffset = 0;
        this.scrollerY = 0;
    }

    protected int saveScrollerY() {
        return (int) Math.max(0, Math.min(travel, scrollerY));
    }

    protected int scrollerOffset() {
        return -((int) (((float) saveScrollerY() / travel) * this.dist));
    }

    public boolean showScrollBar() {
        return child.relativeBounds.height > relativeBounds.height;
    }

    @Override
    public void mouseMoved(double x, double y) {
        if (child != null && relativeBounds.contains(x, y))
            ContainerEventHandler.super.mouseMoved(x - relativeBounds.left, y - relativeBounds.top - scrollerOffset());
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        Rectangle scroller = scrollerRenderer.getScrollerBounds(relativeBounds);
        Rectangle picker = scrollerRenderer.getPickerBounds(scroller, saveScrollerY(), scrollerHeight);
        if (picker.contains((int) x, (int) y)) {
            mouseDown = true;
            mouseDownY = (int) y;
            scrollerDownY = saveScrollerY();
            return true;
        }

        if (child != null && relativeBounds.contains(x, y))
            return ContainerEventHandler.super.mouseClicked(
                    x - relativeBounds.left,
                    y - relativeBounds.top - scrollerOffset(),
                    button
            );
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        mouseDown = false;
        if (child != null && relativeBounds.contains(x, y))
            return ContainerEventHandler.super.mouseReleased(
                    x - relativeBounds.left,
                    y - relativeBounds.top - scrollerOffset(),
                    button
            );
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double x2, double y2) {
        if (mouseDown) {
            int delta = (int) y - mouseDownY;
            scrollerY = scrollerDownY + delta;
            return true;
        }
        if (child != null && relativeBounds.contains(x, y))
            return ContainerEventHandler.super.mouseDragged(
                    x - relativeBounds.left,
                    y - relativeBounds.top - scrollerOffset(),
                    button,
                    x2,
                    y2
            );
        return false;
    }


    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        boolean didCapture = false;
        if (child != null && relativeBounds.contains(x, y)) {
            didCapture = ContainerEventHandler.super.mouseScrolled(
                    x - relativeBounds.left,
                    y - relativeBounds.top - scrollerOffset(),
                    delta
            );
        }
        if (!didCapture) {
            scrollerY = Math.max(0, Math.min(travel, scrollerY)) + delta;
            return true;
        } else {
            System.out.println("Child did capture scroll");
        }
        return false;
    }

    GuiEventListener focused;

    @Nullable
    public GuiEventListener getFocused() {
        return focused;
    }

    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        focused = guiEventListener;
    }

    boolean dragging;

    public boolean isDragging() {
        return this.dragging;
    }

    public void setDragging(boolean bl) {
        dragging = bl;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        if (child != null) {
            if (child.isMouseOver(x - relativeBounds.left, y - relativeBounds.top - scrollerOffset()))
                return true;
        }
        return relativeBounds.contains(x, y);
    }
}
