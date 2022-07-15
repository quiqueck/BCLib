package org.betterx.ui.layout.components;

import org.betterx.ui.layout.values.DynamicSize;

import net.minecraft.client.gui.components.AbstractWidget;

public abstract class AbstractVanillaComponent<C extends AbstractWidget, V extends AbstractVanillaComponent<C, V>> extends Component<AbstractVanillaComponentRenderer<C, V>> {
    protected C vanillaComponent;
    protected final net.minecraft.network.chat.Component component;

    public AbstractVanillaComponent(
            DynamicSize width,
            DynamicSize height,
            AbstractVanillaComponentRenderer<C, V> renderer,
            net.minecraft.network.chat.Component component
    ) {
        super(width, height, renderer);
        this.component = component;
        renderer.linkedComponent = (V) this;
    }

    protected abstract C createVanillaComponent();

    @Override
    protected void onBoundsChanged() {
        vanillaComponent = createVanillaComponent();
    }

    protected net.minecraft.network.chat.Component contentComponent() {
        return component;
    }

    @Override
    public int getContentWidth() {
        return renderer.getWidth(contentComponent());
    }

    @Override
    public int getContentHeight() {
        return renderer.getHeight(contentComponent());
    }

    @Override
    public void mouseMoved(double x, double y) {
        if (vanillaComponent != null)
            vanillaComponent.mouseMoved(x - relativeBounds.left, y - relativeBounds.top);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (vanillaComponent != null)
            return vanillaComponent.mouseClicked(x - relativeBounds.left, y - relativeBounds.top, button);
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (vanillaComponent != null)
            return vanillaComponent.mouseReleased(x - relativeBounds.left, y - relativeBounds.top, button);
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double x2, double y2) {
        if (vanillaComponent != null)
            return vanillaComponent.mouseDragged(
                    x - relativeBounds.left,
                    y - relativeBounds.top,
                    button,
                    x2 - relativeBounds.left,
                    y2 - relativeBounds.top
            );
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double f) {
        if (vanillaComponent != null)
            return vanillaComponent.mouseScrolled(x - relativeBounds.left, y - relativeBounds.top, f);
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (vanillaComponent != null)
            return vanillaComponent.keyPressed(i, j, k);
        return false;
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (vanillaComponent != null)
            return vanillaComponent.keyReleased(i, j, k);
        return false;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (vanillaComponent != null)
            return vanillaComponent.charTyped(c, i);
        return false;
    }

    @Override
    public boolean changeFocus(boolean bl) {
        if (vanillaComponent != null)
            return vanillaComponent.changeFocus(bl);
        return false;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        if (vanillaComponent != null)
            return vanillaComponent.isMouseOver(x, y);
        return false;
    }

}
