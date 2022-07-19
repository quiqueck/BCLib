package org.betterx.ui.layout.components;

import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.components.render.RenderHelper;
import org.betterx.ui.layout.values.Alignment;
import org.betterx.ui.layout.values.Rectangle;
import org.betterx.ui.layout.values.Value;

import com.mojang.blaze3d.vertex.PoseStack;

public class VLine extends CustomRenderComponent {
    private int color = ColorUtil.WHITE;

    public VLine(Value width, Value height) {
        super(width, height);
    }

    public VLine setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected void customRender(PoseStack stack, int x, int y, float deltaTicks, Rectangle bounds, Rectangle clipRect) {
        int left = bounds.height - getContentHeight();
        if (hAlign == Alignment.CENTER) left /= 2;
        else if (hAlign == Alignment.MIN) left = 0;
        RenderHelper.vLine(stack, left, 0, bounds.height, color);
    }

    @Override
    public int getContentWidth() {
        return 1;
    }

    @Override
    public int getContentHeight() {
        return 0;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return false;
    }
}
