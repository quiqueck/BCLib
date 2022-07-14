package org.betterx.ui.layout.components;


import org.betterx.ui.layout.components.input.MouseEvent;
import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.values.DynamicSize;
import org.betterx.ui.layout.values.Rectangle;

import java.util.LinkedList;
import java.util.List;

public class VerticalStack<R extends ComponentRenderer> extends Component<R> {
    protected final List<Component<?>> components = new LinkedList<>();

    public VerticalStack(DynamicSize width, DynamicSize height) {
        this(width, height, null);
    }

    public VerticalStack(DynamicSize width, DynamicSize height, R renderer) {
        super(width, height, renderer);
    }

    @Override
    protected int updateContainerWidth(int containerWidth) {
        int myWidth = width.calculateOrFill(containerWidth);
        for (Component<?> c : components) {
            c.updateContainerWidth(myWidth);
        }
        return myWidth;
    }

    @Override
    public int updateContainerHeight(int containerHeight) {
        int myHeight = height.calculateOrFill(containerHeight);
        int fixedHeight = components.stream().map(c -> c.height.calculate(myHeight)).reduce(0, Integer::sum);

        int freeHeight = Math.max(0, myHeight - fixedHeight);
        fillHeight(myHeight, freeHeight);

        for (Component<?> c : components) {
            c.updateContainerHeight(c.height.calculatedSize());
        }

        return myHeight;
    }

    @Override
    void setRelativeBounds(int left, int top) {
        super.setRelativeBounds(left, top);

        int offset = 0;
        for (Component<?> c : components) {
            c.setRelativeBounds(0, offset);
            offset += c.relativeBounds.height;
        }
    }

    @Override
    public int getContentWidth() {
        return components.stream().map(c -> c.width.calculateFixed()).reduce(0, Integer::max);
    }

    @Override
    public int getContentHeight() {
        int fixedHeight = components.stream().map(c -> c.height.calculateFixed()).reduce(0, Integer::sum);
        double percentage = components.stream().map(c -> c.height.calculateRelative()).reduce(0.0, Double::sum);

        return (int) (fixedHeight / (1 - percentage));
    }

    //    @Override
//    public int fillWidth(int parentSize, int fillSize) {
//        return parentSize;
//    }
    @Override
    public int fillWidth(int parentSize, int fillSize) {
        double totalFillWeight = components.stream().map(c -> c.width.fillWeight()).reduce(0.0, Double::sum);
        return components.stream()
                         .map(c -> c.width.fill(fillSize, totalFillWeight))
                         .reduce(0, Integer::sum);
    }

    @Override
    public int fillHeight(int parentSize, int fillSize) {
        double totalFillHeight = components.stream().map(c -> c.height.fillWeight()).reduce(0.0, Double::sum);
        return components.stream()
                         .map(c -> c.height.fill(fillSize, totalFillHeight))
                         .reduce(0, Integer::sum);
    }

    @Override
    protected void renderInBounds(Rectangle renderBounds, Rectangle clipRect) {
        super.renderInBounds(renderBounds, clipRect);
        for (Component<?> c : components) {
            c.render(renderBounds, clipRect);
        }
    }

    @Override
    void mouseEvent(MouseEvent event, int x, int y) {
        if (!onMouseEvent(event, x, y)) {
            for (Component<?> c : components) {
                c.mouseEvent(event, x - relativeBounds.left, y - relativeBounds.top);
            }
        }
    }

    public VerticalStack<R> add(Component<?> c) {
        this.components.add(c);
        return this;
    }
}
