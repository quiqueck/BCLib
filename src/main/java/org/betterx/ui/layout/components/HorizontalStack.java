package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.*;
import org.betterx.ui.layout.components.input.*;
import org.betterx.ui.layout.values.*;

import java.util.LinkedList;
import java.util.List;

public class HorizontalStack<R extends ComponentRenderer> extends Component<R> {
    protected final List<Component<?>> components = new LinkedList<>();

    public HorizontalStack(DynamicSize width, DynamicSize height) {
        this(width, height, null);
    }

    public HorizontalStack(DynamicSize width, DynamicSize height, R renderer) {
        super(width, height, renderer);
    }

    @Override
    public int updateContainerWidth(int containerWidth) {
        int myWidth = width.calculateOrFill(containerWidth);
        int fixedWidth = components.stream().map(c -> c.width.calculate(myWidth)).reduce(0, Integer::sum);

        int freeWidth = Math.max(0, myWidth - fixedWidth);
        fillWidth(myWidth, freeWidth);

        for (Component<?> c : components) {
            c.updateContainerWidth(c.width.calculatedSize());
        }

        return myWidth;
    }

    @Override
    protected int updateContainerHeight(int containerHeight) {
        int myHeight = height.calculateOrFill(containerHeight);
        for (Component<?> c : components) {
            c.updateContainerHeight(myHeight);
        }
        return myHeight;
    }


    @Override
    void setRelativeBounds(int left, int top) {
        super.setRelativeBounds(left, top);

        int offset = 0;
        for (Component<?> c : components) {
            c.setRelativeBounds(offset, 0);
            offset += c.relativeBounds.width;
        }
    }

    @Override
    public int getContentWidth() {
        int fixedWidth = components.stream().map(c -> c.width.calculateFixed()).reduce(0, Integer::sum);
        double percentage = components.stream().map(c -> c.width.calculateRelative()).reduce(0.0, Double::sum);

        return (int) (fixedWidth / (1 - percentage));
    }

    @Override
    public int getContentHeight() {
        return components.stream().map(c -> c.height.calculateFixed()).reduce(0, Integer::max);
    }

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

//    @Override
//    public int fillHeight(int parentSize, int fillSize) {
//        return parentSize;
//    }

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

    public HorizontalStack<R> add(Component<?> c) {
        this.components.add(c);
        return this;
    }
}
