package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.input.RelativeContainerEventHandler;
import org.betterx.ui.layout.components.render.NullRenderer;
import org.betterx.ui.layout.values.Alignment;
import org.betterx.ui.layout.values.Value;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HorizontalStack extends AbstractStack<NullRenderer, HorizontalStack> implements RelativeContainerEventHandler {
    public HorizontalStack(Value width, Value height) {
        super(width, height);
    }

    @Override
    public int updateContainerWidth(int containerWidth) {
        int myWidth = width.calculateOrFill(containerWidth);
        int fixedWidth = components.stream().map(c -> c.width.calculate(myWidth)).reduce(0, Integer::sum);

        int freeWidth = Math.max(0, myWidth - fixedWidth);
        fillWidth(myWidth, freeWidth);

        for (LayoutComponent<?> c : components) {
            c.updateContainerWidth(c.width.calculatedSize());
        }

        return myWidth;
    }

    @Override
    protected int updateContainerHeight(int containerHeight) {
        int myHeight = height.calculateOrFill(containerHeight);
        components.stream().forEach(c -> c.height.calculateOrFill(myHeight));
        for (LayoutComponent<?> c : components) {
            c.updateContainerHeight(c.height.calculatedSize());
        }
        return myHeight;
    }


    @Override
    void setRelativeBounds(int left, int top) {
        super.setRelativeBounds(left, top);

        int offset = 0;
        for (LayoutComponent<?> c : components) {
            int delta = relativeBounds.height - c.height.calculatedSize();
            if (c.hAlign == Alignment.MIN) delta = 0;
            else if (c.hAlign == Alignment.CENTER) delta /= 2;
            c.setRelativeBounds(offset, delta);
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

    public static HorizontalStack centered(LayoutComponent<?> c) {
        return new HorizontalStack(Value.fill(), Value.fill()).addFiller().add(c).addFiller();
    }

    public static HorizontalStack bottom(LayoutComponent<?> c) {
        return new HorizontalStack(Value.fill(), Value.fill()).add(c).addFiller();
    }

    protected HorizontalStack addEmpty(Value size) {
        this.components.add(new Empty(size, Value.fixed(0)));
        return this;
    }

    public VerticalStack addColumn(Value width, Value height) {
        VerticalStack stack = new VerticalStack(width, height);
        add(stack);
        return stack;
    }


    public VerticalStack addColumn() {
        return addColumn(Value.fit(), Value.fit());
    }
}
