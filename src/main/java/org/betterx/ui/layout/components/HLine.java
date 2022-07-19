package org.betterx.ui.layout.components;

import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.components.render.RenderHelper;
import org.betterx.ui.layout.values.Alignment;
import org.betterx.ui.layout.values.Rectangle;
import org.betterx.ui.layout.values.Value;

import com.mojang.blaze3d.vertex.PoseStack;

public class HLine extends CustomRenderComponent {
    private int color = ColorUtil.DEFAULT_TEXT;

    public HLine(Value width, Value height) {
        super(width, height);
        this.vAlign = Alignment.CENTER;
        this.hAlign = Alignment.CENTER;
    }

    public HLine setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected void customRender(PoseStack stack, int x, int y, float deltaTicks, Rectangle bounds, Rectangle clipRect) {
        int top = bounds.height - getContentHeight();
        if (vAlign == Alignment.CENTER) top /= 2;
        else if (vAlign == Alignment.MIN) top = 0;
        RenderHelper.hLine(stack, 0, bounds.width, top, color);
    }

    @Override
    public int getContentWidth() {
        return 0;
    }

    @Override
    public int getContentHeight() {
        return 1;
    }
}
