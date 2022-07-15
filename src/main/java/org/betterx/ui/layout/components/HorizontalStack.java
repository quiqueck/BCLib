package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.input.RelativeContainerEventHandler;
import org.betterx.ui.layout.components.render.NullRenderer;
import org.betterx.ui.layout.values.Alignment;
import org.betterx.ui.layout.values.DynamicSize;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HorizontalStack extends AbstractStack<NullRenderer, HorizontalStack> implements RelativeContainerEventHandler {
    public HorizontalStack(DynamicSize width, DynamicSize height) {
        super(width, height);
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
        components.stream().forEach(c -> c.height.calculateOrFill(myHeight));
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

    public static HorizontalStack centered(Component<?> c) {
        return new HorizontalStack(DynamicSize.fill(), DynamicSize.fill()).addFiller().add(c).addFiller();
    }

    public static HorizontalStack bottom(Component<?> c) {
        return new HorizontalStack(DynamicSize.fill(), DynamicSize.fill()).add(c).addFiller();
    }

    protected HorizontalStack addEmpty(DynamicSize size) {
        this.components.add(new Empty(size, DynamicSize.fixed(0)));
        return this;
    }
}
