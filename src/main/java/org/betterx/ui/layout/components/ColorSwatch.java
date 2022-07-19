package org.betterx.ui.layout.components;

import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.components.render.RenderHelper;
import org.betterx.ui.layout.values.Rectangle;
import org.betterx.ui.layout.values.Value;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;

public class ColorSwatch extends CustomRenderComponent<ColorSwatch> {
    private int color;
    private int borderColor = ColorUtil.BLACK;
    private boolean offsetInner = false;

    public ColorSwatch(Value width, Value height, int color) {
        super(width, height);
        this.color = color;
    }

    @Override
    protected void customRender(PoseStack stack, int x, int y, float deltaTicks, Rectangle bounds, Rectangle clipRect) {
        int o = offsetInner ? 2 : 1;
        RenderHelper.outline(stack, 0, 0, bounds.width, bounds.height, borderColor);
        GuiComponent.fill(stack, o, o, bounds.width - o, bounds.height - o, color);
    }

    public int getColor() {
        return color;
    }

    public ColorSwatch setColor(int color) {
        this.color = color;
        return this;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public ColorSwatch setBorderColor(int color) {
        this.borderColor = color;
        return this;
    }

    public boolean getOffsetInner() {
        return offsetInner;
    }

    public ColorSwatch setOffsetInner(boolean val) {
        this.offsetInner = val;
        return this;
    }

    @Override
    public int getContentWidth() {
        return 20;
    }

    @Override
    public int getContentHeight() {
        return 20;
    }
}
