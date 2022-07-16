package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.input.RelativeContainerEventHandler;
import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.components.render.NullRenderer;
import org.betterx.ui.layout.values.Rectangle;
import org.betterx.ui.layout.values.Size;
import org.betterx.ui.layout.values.Value;
import org.betterx.ui.vanilla.VanillaScrollerRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class AbstractStack<R extends ComponentRenderer, T extends AbstractStack<R, T>> extends LayoutComponent<R> implements RelativeContainerEventHandler {
    protected final List<LayoutComponent<?>> components = new LinkedList<>();

    public AbstractStack(Value width, Value height) {
        this(width, height, null);
    }

    public AbstractStack(Value width, Value height, R renderer) {
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
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle renderBounds,
            Rectangle clipRect
    ) {
        super.renderInBounds(poseStack, mouseX, mouseY, deltaTicks, renderBounds, clipRect);
        for (LayoutComponent<?> c : components) {
            c.render(poseStack, mouseX, mouseY, deltaTicks, renderBounds, clipRect);
        }
    }

    public T add(LayoutComponent<?> c) {
        this.components.add(c);
        return (T) this;
    }

    protected abstract T addEmpty(Value size);

    public T addSpacer(int size) {
        return addEmpty(Value.fixed(size));
    }

    public T addSpacer(float percentage) {
        return addEmpty(Value.relative(percentage));
    }

    public T addFiller() {
        return addEmpty(Value.fill());
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

    public Image addIcon(ResourceLocation location, Size resourceSize) {
        Image i = new Image(Value.fixed(24), Value.fixed(24), location, resourceSize);
        add(i);
        return i;
    }

    public Image addImage(Value width, Value height, ResourceLocation location, Size resourceSize) {
        Image i = new Image(width, height, location, resourceSize);
        add(i);
        return i;
    }

    public Checkbox addCheckbox(
            Value width, Value height, Component component,
            boolean selected,
            Checkbox.SelectionChanged onSelectionChange
    ) {
        Checkbox c = new Checkbox(width, height, component, selected, true, onSelectionChange);
        add(c);
        return c;
    }

    public Button addButton(
            Value width, Value height,
            Component component,
            net.minecraft.client.gui.components.Button.OnPress onPress
    ) {
        return addButton(width, height, component, onPress, net.minecraft.client.gui.components.Button.NO_TOOLTIP);
    }

    public Button addButton(
            Value width, Value height,
            Component component,
            net.minecraft.client.gui.components.Button.OnPress onPress,
            net.minecraft.client.gui.components.Button.OnTooltip onTooltip
    ) {
        Button b = new Button(width, height, component, onPress, onTooltip);
        add(b);
        return b;
    }

    public VerticalScroll<NullRenderer, VanillaScrollerRenderer> addScrollable(LayoutComponent content) {
        return addScrollable(Value.fill(), Value.fill(), content);
    }

    public VerticalScroll<NullRenderer, VanillaScrollerRenderer> addScrollable(
            Value width,
            Value heihght,
            LayoutComponent content
    ) {
        VerticalScroll<NullRenderer, VanillaScrollerRenderer> s = VerticalScroll.create(width, height, content);
        add(s);
        return s;
    }
}

