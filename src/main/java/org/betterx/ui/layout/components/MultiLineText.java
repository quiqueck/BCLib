package org.betterx.ui.layout.components;

import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.components.render.TextProvider;
import org.betterx.ui.layout.values.Alignment;
import org.betterx.ui.layout.values.Rectangle;
import org.betterx.ui.layout.values.Value;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.MultiLineLabel;

public class MultiLineText extends LayoutComponent<MultiLineText.MultiLineTextRenderer> {
    final net.minecraft.network.chat.Component text;
    int color = ColorUtil.DEFAULT_TEXT;
    protected MultiLineLabel multiLineLabel;

    public MultiLineText(
            Value width,
            Value height,
            final net.minecraft.network.chat.Component text
    ) {
        super(width, height, new MultiLineTextRenderer());
        renderer.linkedComponent = this;
        this.text = text;
    }

    public MultiLineText setColor(int cl) {
        this.color = cl;
        return this;
    }

    protected MultiLineLabel createVanillaComponent() {
        return MultiLineLabel.create(
                renderer.getFont(),
                text,
                relativeBounds == null ? width.calculatedSize() : relativeBounds.width
        );
    }

    @Override
    void setRelativeBounds(int left, int top) {
        super.setRelativeBounds(left, top);
        multiLineLabel = createVanillaComponent();
    }

    @Override
    public int getContentWidth() {
        return renderer.getWidth(text);
    }

    @Override
    public int getContentHeight() {
        return renderer.getHeight(text);
    }

    protected static class MultiLineTextRenderer implements ComponentRenderer, TextProvider {
        MultiLineText linkedComponent;

        @Override
        public int getHeight(net.minecraft.network.chat.Component c) {
            if (linkedComponent == null) return 20;
            MultiLineLabel ml;
            if (linkedComponent.multiLineLabel != null) ml = linkedComponent.multiLineLabel;
            else ml = linkedComponent.createVanillaComponent();
            return ml.getLineCount() * getLineHeight(c);
        }

        @Override
        public void renderInBounds(
                PoseStack stack,
                int mouseX,
                int mouseY,
                float deltaTicks,
                Rectangle bounds,
                Rectangle clipRect
        ) {
            if (linkedComponent != null && linkedComponent.multiLineLabel != null) {

                int top = bounds.height - getHeight(linkedComponent.text);
                if (linkedComponent.vAlign == Alignment.MIN) top = 0;
                if (linkedComponent.vAlign == Alignment.CENTER) top /= 2;

                if (linkedComponent.hAlign == Alignment.CENTER) {
                    linkedComponent.multiLineLabel.renderCentered(
                            stack, bounds.width / 2, top,
                            getLineHeight(linkedComponent.text),
                            linkedComponent.color
                    );
                } else {
                    linkedComponent.multiLineLabel.renderLeftAligned(
                            stack, 0, top,
                            getLineHeight(linkedComponent.text),
                            linkedComponent.color
                    );
                }
            }
        }
    }
}
