package org.betterx.ui.layout.components;


import org.betterx.ui.layout.components.input.RelativeContainerEventHandler;
import org.betterx.ui.layout.components.render.NullRenderer;
import org.betterx.ui.layout.values.Alignment;
import org.betterx.ui.layout.values.Value;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VerticalStack extends AbstractStack<NullRenderer, VerticalStack> implements RelativeContainerEventHandler {
    public VerticalStack(Value width, Value height) {
        super(width, height);
    }

    @Override
    protected int updateContainerWidth(int containerWidth) {
        int myWidth = width.calculateOrFill(containerWidth);
        components.stream().forEach(c -> c.width.calculateOrFill(myWidth));
        for (LayoutComponent<?, ?> c : components) {
            c.updateContainerWidth(c.width.calculatedSize());
        }
        return myWidth;
    }

    @Override
    public int updateContainerHeight(int containerHeight) {
        int myHeight = height.calculateOrFill(containerHeight);
        int fixedHeight = components.stream().map(c -> c.height.calculate(myHeight)).reduce(0, Integer::sum);

        int freeHeight = Math.max(0, myHeight - fixedHeight);
        fillHeight(myHeight, freeHeight);

        for (LayoutComponent<?, ?> c : components) {
            c.updateContainerHeight(c.height.calculatedSize());
        }

        return myHeight;
    }

    @Override
    void setRelativeBounds(int left, int top) {
        super.setRelativeBounds(left, top);

        int offset = 0;
        for (LayoutComponent<?, ?> c : components) {
            int delta = relativeBounds.width - c.width.calculatedSize();
            if (c.hAlign == Alignment.MIN) delta = 0;
            else if (c.hAlign == Alignment.CENTER) delta /= 2;
            c.setRelativeBounds(delta, offset);
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

    public static VerticalStack centered(LayoutComponent<?, ?> c) {
        return new VerticalStack(Value.relative(1), Value.relative(1)).addFiller().add(c).addFiller();
    }

    public static VerticalStack bottom(LayoutComponent<?, ?> c) {
        return new VerticalStack(Value.relative(1), Value.relative(1)).add(c).addFiller();
    }

    protected VerticalStack addEmpty(Value size) {
        this.components.add(new Empty(Value.fixed(0), size));
        return this;
    }

    public HorizontalStack addRow(Value width, Value height) {
        HorizontalStack stack = new HorizontalStack(width, height);
        add(stack);
        return stack;
    }

    public HorizontalStack addRow() {
        return addRow(Value.fit(), Value.fit());
    }
}
