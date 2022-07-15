package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.ComponentRenderer;
import org.betterx.ui.layout.values.DynamicSize;
import org.betterx.ui.layout.values.Rectangle;

import com.mojang.blaze3d.vertex.PoseStack;

public class Text extends Component<Text.TextRenderer> {
    public Text(
            DynamicSize width,
            DynamicSize height
    ) {
        super(width, height, new TextRenderer());
        renderer.linkedComponent = this;
    }

    @Override
    public int getContentWidth() {
        return 0;
    }

    @Override
    public int getContentHeight() {
        return 0;
    }

    public static class TextRenderer implements ComponentRenderer {
        Text linkedComponent;

        @Override
        public void renderInBounds(PoseStack stack, int x, int y, float a, Rectangle bounds, Rectangle clipRect) {

        }
    }
}
