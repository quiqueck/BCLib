package org.betterx.bclib.client.gui.gridlayout;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link org.betterx.ui.vanilla.LayoutScreen} or {@link org.betterx.ui.vanilla.LayoutScreenWithIcon} instead
 */
@Deprecated(forRemoval = true)
@Environment(EnvType.CLIENT)
public abstract class GridScreen extends Screen {
    protected GridLayout grid = null;
    public final int topPadding;
    public final int sidePadding;
    public final boolean centerVertically;
    @Nullable
    public final Screen parent;

    protected int scrollPos = 0;

    public GridScreen(Component title) {
        this(null, title);
    }

    public GridScreen(@Nullable Screen parent, Component title) {
        this(parent, title, 0, true);
    }

    public GridScreen(Component title, int topPadding, boolean centerVertically) {
        this(null, title, topPadding, 20, centerVertically);
    }

    public GridScreen(@Nullable Screen parent, Component title, int topPadding, boolean centerVertically) {
        this(parent, title, topPadding, 20, centerVertically);
    }

    public GridScreen(Component title, int topPadding, int sidePadding, boolean centerVertically) {
        this(null, title, topPadding, sidePadding, centerVertically);
    }

    public GridScreen(
            @Nullable Screen parent,
            Component title,
            int topPadding,
            int sidePadding,
            boolean centerVertically
    ) {
        super(title);

        this.parent = parent;
        this.topPadding = topPadding;
        this.sidePadding = sidePadding;
        this.centerVertically = centerVertically;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    public Font getFont() {
        return this.font;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T guiEventListener) {
        return super.addRenderableWidget(guiEventListener);
    }

    protected void addTitle() {
        grid.addRow().addString(this.title, GridLayout.Alignment.CENTER, this);
        grid.addSpacerRow(15);
    }

    final protected void init() {
        super.init();
        this.grid = new GridLayout(this, this.topPadding, this.sidePadding, this.centerVertically);

        addTitle();

        initLayout();
        grid.finalizeLayout();
    }

    protected abstract void initLayout();

    protected void renderScreen(PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);
    }

    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(i);
        renderGrid(poseStack);
        super.render(poseStack, i, j, f);
    }

    protected void renderGrid(PoseStack poseStack) {
        if (grid != null) {
            if (isScrollable()) {
                for (var item : grid.movableWidgets) {
                    item.first.y = item.second + scrollPos;
                }

                renderScroll(poseStack);

                poseStack.pushPose();
                poseStack.translate(0, scrollPos, 0);
                grid.render(poseStack);
                poseStack.popPose();
            } else {
                grid.render(poseStack);
            }
        }
    }

    public static int getWidth(Component text, Font font) {
        return font.width(text.getVisualOrderText());
    }

    public int getWidth(Component text) {
        return getWidth(text, getFont());
    }

    public void setScrollPos(int sp) {
        scrollPos = Math.max(getMaxScrollPos(), Math.min(0, sp));
    }

    public int getScrollPos() {
        return scrollPos;
    }

    public int getScrollHeight() {
        if (grid != null) return grid.getHeight() + topPadding;
        return height;
    }

    public int getMaxScrollPos() {
        return height - (getScrollHeight() + topPadding);
    }

    public boolean isScrollable() {
        return height < getScrollHeight();
    }

    public boolean isMouseOverScroller(double x, double y) {
        return y >= 0 && y <= height && x >= width - SCROLLER_WIDTH && x <= width;
    }

    private boolean scrolling = false;

    protected void updateScrollingState(double x, double y, int i) {
        this.scrolling = i == 0 && x >= width - SCROLLER_WIDTH && x < width;
    }

    private static final int SCROLLER_WIDTH = 6;

    private void renderScroll(PoseStack poseStack) {
        final int y1 = height;
        final int y0 = 0;
        final int yd = y1 - y0;
        final int maxPosition = getScrollHeight() + topPadding;

        final int x0 = width - SCROLLER_WIDTH;
        final int x1 = width;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        int widgetHeight = (int) ((float) (yd * yd) / (float) maxPosition);
        widgetHeight = Mth.clamp(widgetHeight, 32, yd - 8);
        float relPos = (float) this.getScrollPos() / this.getMaxScrollPos();
        int top = (int) (relPos * (yd - widgetHeight)) + y0;
        if (top < y0) {
            top = y0;
        }

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        //scroller background
        bufferBuilder.vertex(x0, y1, 0.0D).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(x1, y1, 0.0D).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(x1, y0, 0.0D).color(0, 0, 0, 255).endVertex();
        bufferBuilder.vertex(x0, y0, 0.0D).color(0, 0, 0, 255).endVertex();

        //scroll widget shadow
        bufferBuilder.vertex(x0, top + widgetHeight, 0.0D).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(x1, top + widgetHeight, 0.0D).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(x1, top, 0.0D).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(x0, top, 0.0D).color(128, 128, 128, 255).endVertex();

        //scroll widget
        bufferBuilder.vertex(x0, top + widgetHeight - 1, 0.0D)
                     .color(192, 192, 192, 255)
                     .endVertex();
        bufferBuilder.vertex(x1 - 1, top + widgetHeight - 1, 0.0D)
                     .color(192, 192, 192, 255)
                     .endVertex();
        bufferBuilder.vertex(x1 - 1, top, 0.0D).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(x0, top, 0.0D).color(192, 192, 192, 255).endVertex();
        tesselator.end();
    }

    public boolean mouseClicked(double x, double y, int i) {
        this.updateScrollingState(x, y, i);
        if (this.scrolling) {
            return true;
        } else {
            return super.mouseClicked(x, y, i);
        }
    }

    public boolean mouseDragged(double xAbs, double yAbs, int i, double dX, double dY) {
        if (super.mouseDragged(xAbs, yAbs, i, dX, dY)) {
            return true;
        } else if (i == 0 && this.scrolling) {
            if (yAbs < 0) {
                this.setScrollPos(0);
            } else if (yAbs > height) {
                this.setScrollPos(this.getMaxScrollPos());
            } else {
                this.setScrollPos((int) (this.getScrollPos() - dY * 2));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double d, double e, double f) {
        if (isScrollable()) {
            setScrollPos((int) (scrollPos + f * 10));
        }
        return true;
    }

    public boolean keyPressed(int keyCode, int j, int k) {
        if (super.keyPressed(keyCode, j, k)) {
            return true;
        } else if (keyCode == 264) {
            this.mouseScrolled(0, -1.0f, 0);
            return true;
        } else if (keyCode == 265) {
            this.mouseScrolled(0, 1.0, 0);
            return true;
        } else {
            return false;
        }
    }
}
