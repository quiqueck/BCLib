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

    @Override
    public boolean isMouseOver(double x, double y) {
        return bounds.contains(x, y);
    }

//    @Override
//    public void mouseMoved(double x, double y) {
//        if (child != null)
//            child.mouseMoved(x - bounds.left, y - bounds.top);
//    }
//
//    @Override
//    public boolean mouseClicked(double x, double y, int button) {
//        if (child != null)
//            return child.mouseClicked(x - bounds.left, y - bounds.top, button);
//        return false;
//    }
//
//    @Override
//    public boolean mouseReleased(double x, double y, int button) {
//        if (child != null)
//            return child.mouseReleased(x - bounds.left, y - bounds.top, button);
//        return false;
//    }
//
//    @Override
//    public boolean mouseDragged(double x, double y, int button, double x2, double y2) {
//        if (child != null)
//            return child.mouseDragged(x - bounds.left, y - bounds.top, button, x2 - bounds.left, y2 - bounds.top);
//        return false;
//    }
//
//    @Override
//    public boolean mouseScrolled(double x, double y, double f) {
//        if (child != null)
//            return child.mouseScrolled(x - bounds.left, y - bounds.top, f);
//        return false;
//    }
//
//    @Override
//    public boolean keyPressed(int i, int j, int k) {
//        if (child != null)
//            return child.keyPressed(i, j, k);
//        return false;
//    }
//
//    @Override
//    public boolean keyReleased(int i, int j, int k) {
//        if (child != null)
//            return child.keyReleased(i, j, k);
//        return false;
//    }
//
//    @Override
//    public boolean charTyped(char c, int i) {
//        if (child != null)
//            return child.charTyped(c, i);
//        return false;
//    }
//
//    @Override
//    public boolean changeFocus(boolean bl) {
//        if (child != null)
//            return child.changeFocus(bl);
//        return false;
//    }
//
//    @Override
//    public boolean isMouseOver(double x, double y) {
//        if (child != null)
//            return child.isMouseOver(x, y);
//        return false;
//    }
}
