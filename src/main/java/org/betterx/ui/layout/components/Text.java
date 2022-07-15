package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.components.render.TextProvider;
import org.betterx.ui.layout.values.Alignment;
import org.betterx.ui.layout.values.DynamicSize;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;

public class Text extends Component<Text.TextRenderer> {
    final net.minecraft.network.chat.Component text;
    int color = 0xA0A0A0;

    public Text(
            DynamicSize width,
            DynamicSize height,
            net.minecraft.network.chat.Component text

    ) {
        super(width, height, new TextRenderer());
        vAlign = Alignment.CENTER;
        renderer.linkedComponent = this;
        this.text = text;
    }

    public Text setColor(int cl) {
        this.color = cl;
        return this;
    }

    @Override
    public int getContentWidth() {
        return renderer.getWidth(text);
    }

    @Override
    public int getContentHeight() {
        return renderer.getHeight(text);
    }

    public static class TextRenderer implements ComponentRenderer, TextProvider {
        Text linkedComponent;

        @Override
        public int getWidth(net.minecraft.network.chat.Component c) {
            return getFont().width(c.getVisualOrderText());
        }

        @Override
        public int getHeight(net.minecraft.network.chat.Component c) {
            return TextProvider.super.getLineHeight(c);
        }

        @Override
        public void renderInBounds(PoseStack stack, int x, int y, float a, Rectangle bounds, Rectangle clipRect) {
            if (linkedComponent != null) {
                int left = bounds.width - getWidth(linkedComponent.text);
                if (linkedComponent.hAlign == Alignment.MIN) left = 0;
                if (linkedComponent.hAlign == Alignment.CENTER) left /= 2;

                int top = bounds.height - getLineHeight(linkedComponent.text);
                if (linkedComponent.vAlign == Alignment.MIN) top = 0;
                if (linkedComponent.vAlign == Alignment.CENTER) top /= 2;

                GuiComponent.drawString(stack, getFont(), linkedComponent.text, left, top, linkedComponent.color);
            }
        }


    }
}
