package org.betterx.ui.layout.components;


import org.betterx.ui.layout.components.input.RelativeContainerEventHandler;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Panel implements ComponentWithBounds, RelativeContainerEventHandler, NarratableEntry, Widget {
    protected LayoutComponent<?, ?> child;
    List<? extends GuiEventListener> listeners = List.of();
    public final Rectangle bounds;

    public Panel(int width, int height) {
        this(0, 0, width, height);
    }

    public Panel(int left, int top, int width, int height) {
        bounds = new Rectangle(left, top, width, height);
    }

    public void setChild(LayoutComponent<?, ?> c) {
        this.child = c;
        listeners = List.of(c);
    }

    public void calculateLayout() {
        if (child != null) {
            child.updateContainerWidth(bounds.width);
            child.updateContainerHeight(bounds.height);
            child.setRelativeBounds(0, 0);
        }
    }

    @Override
    public Rectangle getRelativeBounds() {
        return bounds;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return listeners;
    }

    @Override
    public Rectangle getInputBounds() {
        return bounds;
    }

    boolean dragging = false;

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

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float deltaTicks) {
        if (child != null) {
            child.render(poseStack, mouseX - bounds.left, mouseY - bounds.top, deltaTicks, bounds, bounds);
        }
    }


}
