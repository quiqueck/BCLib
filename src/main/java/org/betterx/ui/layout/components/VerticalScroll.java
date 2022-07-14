package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.input.MouseEvent;
import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.components.render.ScrollerRenderer;
import org.betterx.ui.layout.values.DynamicSize;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;

public class VerticalScroll<R extends ComponentRenderer, RS extends ScrollerRenderer> extends Component<R> {
    protected Component<?> child;
    protected final RS scrollerRenderer;

    protected int dist;
    protected int scrollerY;
    protected int scrollerHeight;
    protected int travel;
    protected int topOffset;

    public VerticalScroll(DynamicSize width, DynamicSize height, RS scrollerRenderer) {
        this(width, height, scrollerRenderer, null);
    }

    public VerticalScroll(DynamicSize width, DynamicSize height, RS scrollerRenderer, R renderer) {
        super(width, height, renderer);
        this.scrollerRenderer = scrollerRenderer;
    }

    public void setChild(Component<?> c) {
        this.child = c;
    }

    @Override
    protected int updateContainerWidth(int containerWidth) {
        int myWidth = width.calculateOrFill(containerWidth);
        if (child != null) {
            child.width.calculateOrFill(myWidth);
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

    @Override
    public int getContentWidth() {
        return child != null ? child.getContentWidth() : 0;
    }

    @Override
    public int getContentHeight() {
        return child != null ? child.getContentHeight() : 0;
    }

    @Override
    void setRelativeBounds(int left, int top) {
        super.setRelativeBounds(left, top);

        if (child != null)
            child.setRelativeBounds(0, 0);

        updateScrollViewMetrics();
    }

    @Override
    void mouseEvent(MouseEvent event, int x, int y) {
        if (!onMouseEvent(event, x, y)) {
            if (child != null) {
                child.mouseEvent(event, x - relativeBounds.left, y - relativeBounds.top - scrollerOffset());
            }
        }
    }

    @Override
    protected void renderInBounds(PoseStack poseStack, Rectangle renderBounds, Rectangle clipRect) {
        super.renderInBounds(poseStack, renderBounds, clipRect);

        if (showScrollBar()) {
            if (child != null) {
                child.render(
                        poseStack,
                        renderBounds.movedBy(0, scrollerOffset(), scrollerRenderer.scrollerWidth(), 0),
                        clipRect
                );
            }
            scrollerRenderer.renderScrollBar(renderBounds, saveScrollerY(), scrollerHeight);
        } else {
            if (child != null) {
                child.render(poseStack, renderBounds, clipRect);
            }
        }
    }

    private boolean mouseDown = false;
    private int mouseDownY = 0;
    private int scrollerDownY = 0;

    @Override
    public boolean onMouseEvent(MouseEvent event, int x, int y) {
        if (event == MouseEvent.DOWN) {
            Rectangle scroller = scrollerRenderer.getScrollerBounds(relativeBounds);
            Rectangle picker = scrollerRenderer.getPickerBounds(relativeBounds, saveScrollerY(), scrollerHeight);
            if (picker.contains(x, y)) {
                mouseDown = true;
                mouseDownY = y;
                scrollerDownY = saveScrollerY();
                return true;
            }
        } else if (event == MouseEvent.UP) {
            mouseDown = false;
        } else if (event == MouseEvent.MOVE && mouseDown) {
            int delta = y - mouseDownY;
            scrollerY = scrollerDownY + delta;
            return true;
        }
        return mouseDown;
    }

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
        return Math.max(0, Math.min(travel, scrollerY));
    }

    protected int scrollerOffset() {
        return -((int) (((float) saveScrollerY() / travel) * this.dist));
    }

    public boolean showScrollBar() {
        return child.relativeBounds.height > relativeBounds.height;
    }
}
