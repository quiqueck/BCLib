package org.betterx.ui.layout.components;

import org.betterx.ui.layout.values.Value;

import net.minecraft.client.gui.components.AbstractWidget;

public abstract class AbstractVanillaComponent<C extends AbstractWidget, V extends AbstractVanillaComponent<C, V>> extends LayoutComponent<AbstractVanillaComponentRenderer<C, V>, V> {
    protected C vanillaComponent;
    protected final net.minecraft.network.chat.Component component;
    protected float alpha = 1.0f;
    protected boolean enabled = true;

    public AbstractVanillaComponent(
            Value width,
            Value height,
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
        vanillaComponent.setAlpha(this.alpha);
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

    public float getAlpha() {
        return alpha;
    }

    public V setAlpha(float alpha) {
        this.alpha = alpha;
        if (vanillaComponent != null) {
            vanillaComponent.setAlpha(alpha);
        }
        return (V) this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public V setEnabled(boolean enabled) {
        this.enabled = enabled;
        return (V) this;
    }

    @Override
    public void mouseMoved(double x, double y) {
        if (vanillaComponent != null && enabled)
            vanillaComponent.mouseMoved(x - relativeBounds.left, y - relativeBounds.top);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.mouseClicked(x - relativeBounds.left, y - relativeBounds.top, button);
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.mouseReleased(x - relativeBounds.left, y - relativeBounds.top, button);
        return false;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double x2, double y2) {
        if (vanillaComponent != null && enabled)
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
        if (vanillaComponent != null && enabled)
            return vanillaComponent.mouseScrolled(x - relativeBounds.left, y - relativeBounds.top, f);
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.keyPressed(i, j, k);
        return false;
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.keyReleased(i, j, k);
        return false;
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.charTyped(c, i);
        return false;
    }

    @Override
    public boolean changeFocus(boolean bl) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.changeFocus(bl);
        return false;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.isMouseOver(x - relativeBounds.left, y - relativeBounds.top);
        return false;
    }

}
