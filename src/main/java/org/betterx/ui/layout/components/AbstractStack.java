package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.input.RelativeContainerEventHandler;
import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.values.DynamicSize;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class AbstractStack<R extends ComponentRenderer, T extends AbstractStack<R, T>> extends Component<R> implements RelativeContainerEventHandler {
    protected final List<Component<?>> components = new LinkedList<>();

    public AbstractStack(DynamicSize width, DynamicSize height) {
        this(width, height, null);
    }

    public AbstractStack(DynamicSize width, DynamicSize height, R renderer) {
        super(width, height, renderer);
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

    @Override
    protected void renderInBounds(
            PoseStack poseStack,
            int x,
            int y,
            float a,
            Rectangle renderBounds,
            Rectangle clipRect
    ) {
        super.renderInBounds(poseStack, x, y, a, renderBounds, clipRect);
        for (Component<?> c : components) {
            c.render(poseStack, x, y, a, renderBounds, clipRect);
        }
    }

    public T add(Component<?> c) {
        this.components.add(c);
        return (T) this;
    }

    protected abstract T addEmpty(DynamicSize size);

    public T addSpacer(int size) {
        return addEmpty(DynamicSize.fixed(size));
    }

    public T addSpacer(float percentage) {
        return addEmpty(DynamicSize.relative(percentage));
    }

    public T addFiller() {
        return addEmpty(DynamicSize.fill());
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return components;
    }

    @Override
    public Rectangle getInputBounds() {
        return relativeBounds;
    }

    boolean dragging;

    @Override
    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void setDragging(boolean bl) {
        dragging = bl;
    }

    GuiEventListener focused;

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        focused = guiEventListener;
    }
}

